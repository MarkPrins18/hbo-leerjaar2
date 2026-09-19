import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Serializable
data class ExposedUser(val name: String, val age: Int)

class ExposedUserService(val database: Database) {
    object Users : UIntIdTable() {
        val name = varchar("name", length = 50)
        val age = integer("age")
    }

    fun createSchema() {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    fun create(user: ExposedUser): UInt = transaction(database) {
        val newRecord = Users.insert {
            it[name] = user.name
            it[age] = user.age
        }
        newRecord[Users.id].value
    }

    fun read(id: UInt): ExposedUser? {
        return transaction(database) {
            Users.selectAll()
                .where { Users.id eq id }
                .map { ExposedUser(it[Users.name], it[Users.age]) }
                .singleOrNull()
        }
    }

    fun update(id: UInt, user: ExposedUser) {
        transaction(database) {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[age] = user.age
            }
        }
    }

    fun delete(id: UInt) {
        transaction(database) { Users.deleteWhere { Users.id.eq(id) } }
    }

}
