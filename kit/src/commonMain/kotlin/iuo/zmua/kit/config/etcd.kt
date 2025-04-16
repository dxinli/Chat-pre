package iuo.zmua.kit.config

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import okio.Buffer
import io.ktor.websocket.*
import iuo.zmua.kit.encoding.ConfiguredJson
import iuo.zmua.kit.encoding.ConfiguredYaml
import iuo.zmua.kit.http.GlobalClient
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.*
import kotlin.time.Duration.Companion.seconds

@Resource("/v3")
class EtcdApi() {
    @Resource("/kv")
    class KV(val parent: EtcdApi = EtcdApi()){
        @Resource("/range")
        class Range(val parent: KV = KV())
    }

    @Resource("/auth")
    class Auth(val parent: EtcdApi = EtcdApi()){
        @Resource("/authenticate")
        class Login(val parent: Auth = Auth())
    }

    @Resource("/watch")
    class Watch(val parent: EtcdApi = EtcdApi())
}

class EtcdClient(
   internal var httpClient: HttpClient,
)

object TokenManager  {
    private var currentToken: String? = null
    private var expiryTime: Long = 0
    private val mutex = Mutex()
    private var refreshJob: Job? = null
    private lateinit var etcdClient: EtcdClient
    private var username = "root"
    private var password = "sakura"

    @OptIn(ExperimentalTime::class)
    private suspend fun fetchNewToken() {
        currentToken = etcdClient.auth(username, password)
        // 使用固定 TTL（根据 etcd 服务端默认配置）
        val ttl = 300L  // 默认 300 秒（5 分钟）
        expiryTime = (Clock.System.now()+ttl.seconds).toEpochMilliseconds()
    }

    suspend fun getToken(): String {
        mutex.withLock {
            return currentToken ?: run {
                fetchNewToken()
                currentToken!!
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun init(etcdClient: EtcdClient,username: String,password: String) = mutex.withLock {
        this.etcdClient = etcdClient
        this.username = username
        this.password = password
        if (refreshJob == null) {
            refreshJob = CoroutineScope(Dispatchers.Unconfined).launch {
                while (true) {
                    delay(30.seconds)
                    if (Clock.System.now().toEpochMilliseconds() >= expiryTime - 30_000) {
                        fetchNewToken()
                    }
                }
            }
        }
        fetchNewToken()
    }
}

// 修改单例获取方式为延迟初始化
val etcdClientInstance by lazy {
    CoroutineScope(Dispatchers.Default).async {
        etcdClient() // 异步初始化
    }
}

private suspend fun etcdClient(): EtcdClient {
    val etcdClient = EtcdClient(GlobalClient.config {
        install(WebSockets){
            pingIntervalMillis = 20_000
        }
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = 5000
        }
        defaultRequest {
            url {
                host = "localhost"
                port = 2379
            }
            contentType(ContentType.Application.Json)
        }
    })
    TokenManager.init(etcdClient,"root","sakura")
    return etcdClient
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun EtcdClient.auth(username:String, password:String): String {
    val res = httpClient.post(EtcdApi.Auth.Login()) {
        setBody(mapOf("name" to username, "password" to password))
    }
    if (res.status.value in 200..299) {
        val authJson = ConfiguredJson.parseToJsonElement(res.bodyAsText()).jsonObject
        println("auth ok,response:$authJson")
        authJson["token"]?.jsonPrimitive?.content?.let {
            return it
        }
    }
    throw Exception("auth fail,res status ${res.status.value},response:$res")
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun EtcdClient.configLoad(keyStr: String):Buffer{
    val token = TokenManager.getToken()
    println("etcd config load,auth token:${token}")
    val key = Base64.encode(keyStr.encodeToByteArray())
    val res = httpClient.post(EtcdApi.KV.Range()){
        headers {
            append("Authorization", token)
        }

        setBody(mapOf("key" to key))
    }
    if (res.status.value in 200..299) {
        println(res)
        val config = res.body<JsonObject>()
        println(config)
        config["kvs"]?.let { jsonElement ->
            val value = jsonElement.jsonArray[0].jsonObject["value"]?.jsonPrimitive?.content
            value?.let {
                val base64 = Base64.decode(it)
                return Buffer().write(base64)
            }
        }
        throw Exception("etcd config unknown key, key: $keyStr")
    }
    throw Exception("etcd config load error , status: ${res.status},message:${res.body<String>()}")
}

suspend inline fun <reified T> EtcdClient.configLoadAndWatch(keyStr: String, crossinline onUpdate: (T) -> Unit) : T {
    println("etcd config load")
    val configBuffer = configLoad(keyStr)
    val initialConfig  = ConfiguredYaml.decodeFromSource<T>(configBuffer)
    // 启动监听（在后台协程）
    CoroutineScope(Dispatchers.Default).launch {
        watch(keyStr) { rawValue ->
            try {
                val newConfig = ConfiguredYaml.decodeFromSource<T>(rawValue)
                onUpdate(newConfig)
            } catch (e: Exception) {
                println("Failed to reload config: ${e.message}")
            }
        }
    }
    return initialConfig
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun EtcdClient.watchWebSocket(
    keyStr: String,
    onUpdate: (String) -> Unit
) {
    println("etcd config watch")
    val token = TokenManager.getToken()
    println("etcd config watch token: $token")
    val watchKey = Base64.encode(keyStr.encodeToByteArray())
    httpClient.ws(method = HttpMethod.Get, path = "/v3/watch",request = {
        // 添加认证 header
        header(HttpHeaders.SecWebSocketProtocol,"Bearer, $token")
    }){
        send(Frame.Text("""{
                    "create_request": {
                        "key": "$watchKey",
                        "start_revision": 0,
                        "progress_notify": true,
                        "filters": []
                    }
                }
                """.trimIndent()
        ))

        // 接收事件
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        println("etcd config watch text: $text")
                        onUpdate(text)
                    }
                    is Frame.Close -> throw CancellationException("WebSocket closed")
                    else -> { /* ignore */ }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw Exception("WebSocket error", e)
        }
    }
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalSerializationApi::class)
suspend fun EtcdClient.watch(
    keyStr: String,
    onUpdate: (Buffer) -> Unit
) {
    println("etcd config watch")
    val token = TokenManager.getToken()
    println("etcd config watch token: $token")
    val watchKey = Base64.encode(keyStr.encodeToByteArray())
    httpClient.preparePost(EtcdApi.Watch()){
        headers {
            header(HttpHeaders.Authorization, token)
            header(HttpHeaders.Connection, "keep-alive")
        }
        setBody("""{
                    "create_request": {
                        "key": "$watchKey",
                        "start_revision": 0,
                        "progress_notify": true,
                        "filters": []
                    }
                }
                """.trimIndent()
        )
    }.execute { res ->
        println("call is done,res: $res")
        // 读取 chunked response
        val channel = res.bodyAsChannel()
        try {
            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break

                val json = ConfiguredJson.parseToJsonElement(line).jsonObject
                val result = json["result"]
                println(result)
                result?.jsonObject?.get("events")?.jsonArray?.mapNotNull {
                    println(it)
                    it.jsonObject["kv"]?.jsonObject?.get("value")?.jsonPrimitive?.content
                }?.forEach { base64Value ->
                    onUpdate(Buffer().write(Base64.decode(base64Value)))
                }

                // 错误处理应基于响应头
                if (result?.jsonObject?.get("canceled")?.jsonPrimitive?.content == "true") {
                    throw Exception("Watch cancel: ${result.jsonObject["cancel_reason"]?.jsonPrimitive?.content}")
                }
            }
        }catch (e: Exception) {
            channel.cancel(e)
            throw e
        }
    }
}
