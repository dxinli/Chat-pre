package iuo.zmua.demo.external

import iuo.zmua.api.User
import iuo.zmua.server.UserServer
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RSocketClient(
    private val userServer: UserServer,
    rsocketRequesterBuilder: RSocketRequester.Builder
){

    @GetMapping("/user")
    suspend fun getMe(): User {
        return userServer.getMe()
    }
}