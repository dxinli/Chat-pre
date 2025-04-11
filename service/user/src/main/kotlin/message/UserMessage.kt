package iuo.zmua.user.message

import iuo.zmua.api.User
import iuo.zmua.server.UserServer
import org.springframework.stereotype.Controller

@Controller
class UserMessage : UserServer {

    override suspend fun getMe(): User {
        val user = User(1, "yangqiyu")
        println(user)
        return user
    }

    override suspend fun deleteMe() {
        TODO("Not yet implemented")
    }

    override suspend fun all(): List<User> {
        TODO("Not yet implemented")
    }

}