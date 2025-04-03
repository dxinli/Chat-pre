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

suspend fun apiClient(config: RSocketConfig):ApiClient {
    val client = HttpClient {
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

    val rSocket = client.rSocket(config.target.host,config.target.port,config.target.path)
    return ApiClient(rSocket, Codec(config.connector.dataMimeType))
}

// 将发送消息的方法委托给 rSocket，并将 codec 暴露
class ApiClient internal constructor(
    val rSocket: RSocket, val codec: Codec
): RSocket by rSocket




