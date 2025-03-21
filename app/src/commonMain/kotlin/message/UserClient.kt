package message

import io.rsocket.kotlin.RSocket
import iuo.zmua.api.User
import iuo.zmua.api.UserApi
import iuo.zmua.api.utils.Payload
import iuo.zmua.api.utils.decodeFromPayload
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
class UserClient(private val rSocket: RSocket, private val proto: ProtoBuf) : UserApi{

    override suspend fun getMe(): User = proto.decodeFromPayload(
        rSocket.requestResponse(Payload(route = "users.getMe"))
    )

    override suspend fun deleteMe() {
        rSocket.fireAndForget(Payload(route = "users.deleteMe"))
    }

    override suspend fun all(): List<User> = proto.decodeFromPayload(
        rSocket.requestResponse(Payload(route = "users.all"))
    )

}