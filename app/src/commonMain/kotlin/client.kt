package iuo.zmua.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.ConnectionOptions.Companion.KeepAlive
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.core.WellKnownMimeType
//import io.rsocket.kotlin.keepalive.KeepAlive
//import io.rsocket.kotlin.ktor.client.RSocketSupport
//import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.PayloadMimeType
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import io.rsocket.kotlin.transport.ktor.tcp.KtorTcpClientTransport
import io.rsocket.kotlin.transport.ktor.websocket.client.KtorWebSocketClientTransport
import iuo.zmua.api.utils.ConfiguredProtoBuf
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.ExperimentalSerializationApi
import message.UserClient
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalSerializationApi::class)
class ApiClient(rSocket: RSocket) {
    private val proto = ConfiguredProtoBuf
    val users = UserClient(rSocket, proto)
}


enum class TransportType { TCP, WS }

data class ServerAddress(val port: Int, val type: TransportType)

object Servers {
    private val JS = listOf(
//    ServerAddress(port = 9051, type = TransportType.TCP),
        ServerAddress(port = 8087, type = TransportType.WS),
    )

    val ALL = JS
}

suspend fun runClient(
    addresses: List<ServerAddress>,
    name: String,
    target: String,
): Unit = supervisorScope {
    addresses.forEach { address ->
        launch {
            val client = ApiClient(coroutineContext, address, name)
//            val message = "RSocket is awesome! (from $target)"
            val user = client.users.getMe()
            println(user)
//            val chat = client.chats.all().firstOrNull() ?: client.chats.new("rsocket-kotlin chat")
//
//            val sentMessage = client.messages.send(chat.id, message)
//            println("Send to [$address]: $sentMessage")
//
//            client.messages.messages(chat.id, -1).collect {
//                println("Received from [$address]: $it")
//            }
        }
    }
}

private suspend fun ApiClient(
    context: CoroutineContext,
    address: ServerAddress,
    name: String,
): ApiClient {
    println("Connecting client to: $address")
    val connector = RSocketConnector {
        connectionConfig {
            setupPayload { buildPayload { data(name) } }
            //mime types
            payloadMimeType = PayloadMimeType(
                data = WellKnownMimeType.ApplicationJson,
                metadata = WellKnownMimeType.MessageRSocketRouting
            )
        }
    }

    val target =  KtorWebSocketClientTransport(context) {
        httpEngine(CIO)
    }.target(host = "localhost", port = address.port,path = "/rsocket")
//    val client = HttpClient {
//        install(WebSockets) // rsocket requires websockets plugin installed
//        install(RSocketSupport) {
//            // configure rSocket connector (all values have defaults)
//            connector {
//                connectionConfig {
//                    // payload for setup frame
//                    setupPayload { buildPayload { data(name) } }
//                    // mime types
//                    payloadMimeType = PayloadMimeType(
//                        data = WellKnownMimeType.ApplicationJson,
//                        metadata = WellKnownMimeType.MessageRSocketRouting
//                    )
//                }
//            }
//        }
//    }
//    val rSocket = client.rSocket("localhost",8087,"/rsocket")
    val rSocket = connector.connect(target)
    return ApiClient(rSocket)
}