package iuo.zmua.server

import iuo.zmua.api.User
import iuo.zmua.api.UserApi
import org.springframework.messaging.rsocket.service.RSocketExchange

@RSocketExchange("api.v1.user")
interface UserServer : UserApi {

    @RSocketExchange("getMe")
    override suspend fun getMe(): User

    @RSocketExchange("deleteMe")
    override suspend fun deleteMe()

    @RSocketExchange("all")
    override suspend fun all(): List<User>
}
