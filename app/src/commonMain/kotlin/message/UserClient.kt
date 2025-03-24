package message

import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.compositeMetadata
import io.rsocket.kotlin.payload.buildPayload
import iuo.zmua.api.User
import iuo.zmua.api.UserApi
import iuo.zmua.api.utils.Payload
import iuo.zmua.api.utils.decodeFromPayload
import kotlinx.io.Buffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
class UserClient(private val rSocket: RSocket, private val proto: ProtoBuf) : UserApi{

    @OptIn(ExperimentalMetadataApi::class)
    override suspend fun getMe(): User {
        println("调用开始")
        return proto.decodeFromPayload(
//            rSocket.requestResponse(Payload(route = "api.v1.user.getMe"))
            rSocket.requestResponse(buildPayload{
                data(Buffer())
                compositeMetadata {
                    add(RoutingMetadata("api.v1.user.getMe"))
                }
            })
        )
    }

    override suspend fun deleteMe() {
        rSocket.fireAndForget(Payload(route = "api.v1.user.deleteMe"))
    }

    override suspend fun all(): List<User> = proto.decodeFromPayload(
        rSocket.requestResponse(Payload(route = "api.v1.user.all"))
    )

}