package iuo.zmua.user.message

import iuo.zmua.api.User
import iuo.zmua.api.UserApi
import org.springframework.stereotype.Controller

@Controller
class UserMessage : UserApi{

    override suspend fun getMe(): User = User(1,"yangqiyu")

    override suspend fun deleteMe() {
        TODO("Not yet implemented")
    }

    override suspend fun all(): List<User> {
        TODO("Not yet implemented")
    }

}