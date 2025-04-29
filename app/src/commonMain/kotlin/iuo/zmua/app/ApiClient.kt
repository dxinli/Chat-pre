package iuo.zmua.app

import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import io.rsocket.kotlin.transport.ktor.websocket.client.KtorWebSocketClientTransport
import iuo.zmua.kit.config.RSocketConfig
import iuo.zmua.codec.Codec
import iuo.zmua.kit.config.EtcdClient
import iuo.zmua.kit.config.EtcdConfig
import iuo.zmua.kit.config.RSocketConnectorConfig
import iuo.zmua.kit.config.TransportTargetConfig
import iuo.zmua.kit.config.configLoadAndWatch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private fun createConnector(config: RSocketConnectorConfig) = RSocketConnector {
    connectionConfig {
        // payload for setup frame
        setupPayload { buildPayload { data("""{ "data": "setup" }""") } }
        // mime types
        payloadMimeType = PayloadMimeType(
            data = config.dataMimeType,
            metadata = config.metadataMimeType
        )
    }
}

private suspend fun createTarget(config: TransportTargetConfig) = KtorWebSocketClientTransport(coroutineContext)
    .target(host = config.host, port = config.port, path = config.path)

private suspend fun rSocket(config: RSocketConfig): RSocket{
    val connector = createConnector(config.connector)
    val target = createTarget(config.target)
    println("connect to target: ${config.target}")

    return try {
        connector.connect(target)
    } catch (e: Exception) {
        // 降级处理，调用时抛出异常服务不可用
        object : RSocket {
            override val coroutineContext: CoroutineContext
                get() = target.coroutineContext

            override suspend fun requestResponse(payload: Payload): Payload {
                throw Exception("Service is unavailable")
            }

            override suspend fun fireAndForget(payload: Payload) {
                throw Exception("Service is unavailable")
            }

            override fun requestStream(payload: Payload): Flow<Payload> {
                throw Exception("Service is unavailable")
            }

            override fun requestChannel(initPayload: Payload, payloads: Flow<Payload>): Flow<Payload> {
                throw Exception("Service is unavailable")
            }
        }
    }
}

suspend fun apiClient():ApiClient {
    val etcdClient = EtcdClient.create(EtcdConfig())
    var apiClient:ApiClient?=null
    val config:RSocketConfig = etcdClient.configLoadAndWatch<RSocketConfig>("rSocket") { newConfig ->
        apiClient?.onConfigUpdate(newConfig)
    }

    return ApiClient(
        rSocket = rSocket(config),
        codec = Codec(config.connector.dataMimeType),
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ).also { apiClient = it }
}

// 将发送消息的方法委托给 rSocket，并将 codec 暴露
class ApiClient internal constructor(
    var rSocket: RSocket,
    var codec: Codec,
    private val scope: CoroutineScope,
): RSocket by rSocket{
    fun onConfigUpdate(newConfig:RSocketConfig) {
        scope.launch { // 在协程作用域中执行挂起操作
            val newRSocket = rSocket(newConfig)
            rSocket = newRSocket
            codec = Codec(newConfig.connector.dataMimeType)
        }
    }
}


