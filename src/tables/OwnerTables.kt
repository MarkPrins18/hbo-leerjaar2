package tables

import org.jetbrains.exposed.v1.core.Table

object OwnerTable : Table("owner") {
    val id = integer("id").autoIncrement()

    override val primaryKey = PrimaryKey(id)
}