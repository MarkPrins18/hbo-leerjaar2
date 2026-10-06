package repositories

import models.User
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.UserTable

class ExposedUserRepository : UserRepository {

    override suspend fun findByEmail(email: String): User? = suspendTransaction {
        UserTable.selectAll().where { UserTable.email eq email }.singleOrNull()?.toUser()
    }

    override suspend fun findAll(): List<User> = suspendTransaction {
        UserTable.selectAll().orderBy(UserTable.id).map { it.toUser() }
    }

    override suspend fun create(email: String, passwordHash: String): User = suspendTransaction {
        val inserted = UserTable.insert {
            it[UserTable.email] = email
            it[UserTable.passwordHash] = passwordHash
        }
        User(id = inserted[UserTable.id], email = email, passwordHash = passwordHash)
    }

    private fun ResultRow.toUser() = User(
        id = this[UserTable.id],
        email = this[UserTable.email],
        passwordHash = this[UserTable.passwordHash]
    )
}
