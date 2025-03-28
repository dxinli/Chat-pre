package iuo.zmua.application

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.metadata
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import io.rsocket.kotlin.transport.ktor.websocket.client.KtorWebSocketClientTransport
import iuo.zmua.api.iuo.zmua.api.User
import iuo.zmua.api.utils.ConfiguredJson
import iuo.zmua.api.utils.ConfiguredProtoBuf
import iuo.zmua.api.utils.decodeFromPayload
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.coroutines.coroutineContext

fun main() = runBlocking {
    runClient()
}

@OptIn(ExperimentalMetadataApi::class, ExperimentalSerializationApi::class)
suspend fun runClientBak() {
    //create ktor client
    val client = HttpClient {
        install(WebSockets) // rsocket requires websockets plugin installed
        install(RSocketSupport) {
            // configure rSocket connector (all values have defaults)
            connector {
                connectionConfig {
                    // payload for setup frame
                    setupPayload {
                        buildPayload {
                            data("""{ "data": "setup" }""")
                        }
                    }

                    // mime types
                    payloadMimeType = PayloadMimeType(
                        data = WellKnownMimeType.ApplicationJson,
                        metadata = WellKnownMimeType.MessageRSocketRouting
                    )

                }
            }
        }
    }
    val rSocket = client.rSocket("localhost",8087,"/rsocket")
    val payload = buildPayload {
        data(Buffer())
        metadata(RoutingMetadata("api.v1.user.getMe"))
    }
    val data = rSocket.requestResponse(payload)
    val user = ConfiguredProtoBuf.decodeFromPayload<User>(data)
    println(user)
}

@OptIn(ExperimentalSerializationApi::class, ExperimentalMetadataApi::class)
suspend fun runClient() {
    val connector = RSocketConnector {
        connectionConfig {
            setupPayload { buildPayload { data("Oleg") } }
            //mime types
            payloadMimeType = PayloadMimeType(
                data = WellKnownMimeType.ApplicationJson,
                metadata = WellKnownMimeType.MessageRSocketRouting
            )
        }
    }
    val target = KtorWebSocketClientTransport(coroutineContext) {
        httpEngine(CIO)
    }.target(host = "localhost", port = 8087, path = "/rsocket")
//    val target = KtorTcpClientTransport(coroutineContext).target(host = "127.0.0.1", port = 8087)
    val rSocket = connector.connect(target)
    val payload = buildPayload {
        data(Buffer())
        metadata(RoutingMetadata("api.v1.user.getMe"))
    }
    val data = rSocket.requestResponse(payload)
    val user = ConfiguredJson.decodeFromPayload<User>(data)
    println(user)
}