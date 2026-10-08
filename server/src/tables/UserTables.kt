package tables

import org.jetbrains.exposed.v1.core.Table

val userTables = listOf(UserTable)

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 60)

    override val primaryKey = PrimaryKey(id)
}
