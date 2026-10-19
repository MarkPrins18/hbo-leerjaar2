package tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

val photoTables = listOf(PhotoTable)

object PhotoTable : Table("photo") {
    val id = integer("id").autoIncrement()
    val carId = integer("car_id") references CarTable.id
    val fileName = varchar("file_name", 100)
    val uploadedAt = timestamp("uploaded_at")

    override val primaryKey = PrimaryKey(id)
}
