package repositories

import models.User

interface UserRepository {
    suspend fun findByEmail(email: String): User?
    suspend fun findAll(): List<User>
    suspend fun userExists(userId: Int): Boolean
    suspend fun create(email: String, passwordHash: String): User
}
