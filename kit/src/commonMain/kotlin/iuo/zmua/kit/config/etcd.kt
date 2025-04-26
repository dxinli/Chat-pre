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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
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

data class EtcdConfig(
    val username: String = "root",
    val password: String = "sakura",
    val host: String = "localhost",
    val port: Int = 2379,
    val ttl: Duration = 300.seconds,
)


class EtcdClient private constructor(
    internal var httpClient: HttpClient,
){

    fun userName() = config.username

    fun password() = config.password

    companion object {

        private var config = EtcdConfig()

        private val etcdClientInstance:EtcdClient by lazy {
            EtcdClient(GlobalClient.config {
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
                        this.host = config.host
                        this.port = config.port
                    }
                    contentType(ContentType.Application.Json)
                }
            })
        }

        suspend fun create(config: EtcdConfig): EtcdClient {
            this.config = config
            val refreshJob = TokenManager.init(etcdClientInstance)
            try {
                TokenManager.initializationDeferred.await()
            } catch (e: Exception) {
                // 处理初始化失败（如重试或终止）
                refreshJob?.cancel()
                throw Exception("TokenManager init fail", e)
            }
            if (refreshJob?.isActive != true) {
                throw Exception("TokenManager refresh job not active")
            }
            return etcdClientInstance
        }

    }
}

object TokenManager  {
    private var currentToken: String? = null
    private var expiryTime: Long = 0
    private val mutex = Mutex()
    private var refreshJob: Job? = null
    private lateinit var etcdClient: EtcdClient
    internal val initializationDeferred = CompletableDeferred<Unit>()  // 新增：初始化完成标记

    @OptIn(ExperimentalTime::class)
    private suspend fun fetchNewToken() {
        currentToken = etcdClient.auth(etcdClient.userName(), etcdClient.password())
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
    suspend fun init(etcdClient: EtcdClient):Job? {
        mutex.withLock {
            this.etcdClient = etcdClient
            if (refreshJob == null) {
                println("create refresh token job")
                refreshJob = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e ->
                    initializationDeferred.completeExceptionally(e)
                }).launch {
                    try {
                        fetchNewToken() // 首次 Token 获取
                        initializationDeferred.complete(Unit)  // 标记初始化完成
                        while (isActive) {
                            delay(30.seconds)
                            if (Clock.System.now().toEpochMilliseconds() >= expiryTime - 30_000) {
                                fetchNewToken()
                            }
                        }
                    }catch (e:CancellationException){
                        println("refresh token job cancelled")
                    }catch (e: Exception) {
                        println("refresh token  error: ${e.message}")
                        throw e
                    }
                }

            }
            println("init finished")
            return refreshJob
        }
    }
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
                println("etcd config load ok,value:${Buffer().write(base64).readByteString().utf8()}")
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
                val rawConfig = rawValue.readByteString().utf8()
                val newConfig = ConfiguredYaml.decodeFromString<T>(rawConfig)
                println("etcd config reload:new config:\n${newConfig}")
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
    onUpdate: (Buffer) -> Unit,
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
