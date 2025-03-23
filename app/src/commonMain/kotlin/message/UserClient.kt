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

    override suspend fun getMe(): User {
        println("调用开始")
        return proto.decodeFromPayload(
            rSocket.requestResponse(Payload(route = "api.v1.user.getMe"))
        )
    }

    override suspend fun deleteMe() {
        rSocket.fireAndForget(Payload(route = "api.v1.user.deleteMe"))
    }

    override suspend fun all(): List<User> = proto.decodeFromPayload(
        rSocket.requestResponse(Payload(route = "api.v1.user.all"))
    )

}