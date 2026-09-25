package tables

import org.jetbrains.exposed.v1.core.Table

object PhotoTable : Table("photo") {
    val id = integer("id").autoIncrement()
    val carId = integer("car_id") references CarTable.id
    val url = varchar("url", 500)
    val uploadedAt = long("uploaded_at")

    override val primaryKey = PrimaryKey(id)
}

