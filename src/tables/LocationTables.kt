package tables

import org.jetbrains.exposed.v1.core.Table

object LocationTable : Table("location") {
    val id = integer("id").autoIncrement()
    val carId = integer("car_id") references CarTable.id
    val userId = (integer("user_id") references RenterTable.id).nullable()
    val latitude = double("latitude")
    val longitude = double("longitude")
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}

