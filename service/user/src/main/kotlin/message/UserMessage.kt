package iuo.zmua.user.message

import iuo.zmua.api.User
import iuo.zmua.server.UserServer
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties
import org.springframework.stereotype.Controller

@Controller
class UserMessage(val properties: RSocketProperties) : UserServer {

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

    override suspend fun addUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun batchAddUser(users: List<User>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(userId: String): User? {
        TODO("Not yet implemented")
    }

}