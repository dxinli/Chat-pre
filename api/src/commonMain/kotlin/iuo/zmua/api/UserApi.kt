package iuo.zmua.api

import kotlinx.serialization.*

interface UserApi {

    suspend fun getMe(): User

    suspend fun deleteMe()

    suspend fun all(): List<User>

    suspend fun addUser(user: User)

    suspend fun batchAddUser(users: List<User>)

    suspend fun updateUser(user: User)

    suspend fun getUser(userId: String): User?
}

@Serializable
data class User(
    val id: Int,
    val name: String,
)
