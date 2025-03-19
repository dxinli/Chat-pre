package iuo.zmua.api

import kotlinx.serialization.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.rsocket.service.RSocketExchange

@MessageMapping("api.v1.user")
interface UserApi {

    @RSocketExchange("getMe")
    suspend fun getMe(): User

    @RSocketExchange("deleteMe")
    suspend fun deleteMe()

    @RSocketExchange("all")
    suspend fun all(): List<User>
}

@Serializable
data class User(
    val id: Int,
    val name: String,
)
