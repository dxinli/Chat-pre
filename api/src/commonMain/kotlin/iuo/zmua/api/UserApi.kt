package iuo.zmua.api

import iuo.zmua.kit.RSocketApi
import kotlinx.serialization.*

@RSocketApi("api.v1.user")
interface UserApi {

    @RSocketApi("getMe")
    suspend fun getMe(): User

    @RSocketApi("deleteMe")
    suspend fun deleteMe()

    @RSocketApi("all")
    suspend fun all(): List<User>
}

@Serializable
data class User(
    val id: Int,
    val name: String,
)
