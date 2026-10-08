package tables

import org.jetbrains.exposed.v1.core.Table

val ownerTables = listOf(OwnerTable)

object OwnerTable : Table("owner") {
    val id = integer("id").autoIncrement()

    override val primaryKey = PrimaryKey(id)
}