package iuo.zmua.app.message

import iuo.zmua.api.User
import iuo.zmua.api.UserApi
import iuo.zmua.app.ApiClient
import iuo.zmua.app.extensions.requestResponse
import iuo.zmua.app.extensions.fireAndForget

class UserClient(private val api:ApiClient) : UserApi {

    override suspend fun getMe(): User {
        println("调用开始")
        return api.requestResponse<User>("api.v1.user.deleteMe")
    }

    override suspend fun deleteMe() {
        api.fireAndForget("api.v1.user.deleteMe")
    }

    override suspend fun all(): List<User> {
        println("调用开始")
        return api.requestResponse<List<User>>("api.v1.user.deleteMe","yanghongzhong")
    }

}