package iuo.zmua.app.message

import iuo.zmua.api.User
import iuo.zmua.api.UserApi
import iuo.zmua.app.ApiClient
import iuo.zmua.app.extensions.requestResponse
import iuo.zmua.app.extensions.fireAndForget

class UserClient(private val api:ApiClient) : UserApi {

    override suspend fun getMe(): User {
        println("调用开始")
        return api.requestResponse<User>("user.getMe")
    }

    override suspend fun deleteMe() {
        api.fireAndForget("user.deleteMe")
    }

    override suspend fun all(): List<User> {
        println("调用开始")
        return api.requestResponse<List<User>>("user.deleteMe","yanghongzhong")
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