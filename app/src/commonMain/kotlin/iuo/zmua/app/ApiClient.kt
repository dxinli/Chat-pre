package iuo.zmua.app

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import iuo.zmua.kit.config.RSocketConfig
import iuo.zmua.codec.Codec
import iuo.zmua.kit.config.EtcdClient
import iuo.zmua.kit.config.EtcdConfig
import iuo.zmua.kit.config.configLoadAndWatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private fun buildHttpClient(config: RSocketConfig) = HttpClient {
    install(WebSockets) // rsocket requires websockets plugin installed
    install(RSocketSupport) {
        // configure rSocket connector (all values have defaults)
        connector {
            connectionConfig {
                // payload for setup frame
                setupPayload { buildPayload { data("""{ "data": "setup" }""") } }
                // mime types
                payloadMimeType = PayloadMimeType(
                    data = config.connector.dataMimeType,
                    metadata = config.connector.metadataMimeType
                )
            }
        }
    }
}

private suspend fun createRSocket(config: RSocketConfig) = buildHttpClient(config).rSocket(
    host = config.target.host,
    port = config.target.port,
    path = config.target.path
)

suspend fun apiClient():ApiClient {
    val etcdClient = EtcdClient.create(EtcdConfig())
    var apiClient:ApiClient?=null
    val config:RSocketConfig = etcdClient.configLoadAndWatch<RSocketConfig>("rSocket") { newConfig ->
        apiClient?.onConfigUpdate(newConfig)
    }
    // 首次创建客户端
    return ApiClient(
        rSocket = createRSocket(config),
        codec = Codec(config.connector.dataMimeType),
        client = buildHttpClient(config),
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    ).also { apiClient = it }
}

// 将发送消息的方法委托给 rSocket，并将 codec 暴露
class ApiClient internal constructor(
    var rSocket: RSocket,
    var codec: Codec,
    private val client:HttpClient,
    private val scope: CoroutineScope,
): RSocket by rSocket{
    fun onConfigUpdate(newConfig:RSocketConfig) {
        scope.launch { // 在协程作用域中执行挂起操作
            val newRSocket = createRSocket(newConfig)
            client.close() // 关闭旧客户端
            rSocket = newRSocket
            codec = Codec(newConfig.connector.dataMimeType)
        }
    }
}




