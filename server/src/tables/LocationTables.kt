package tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

val locationTables = listOf(LocationTable)

object LocationTable : Table("location") {
    val id = integer("id").autoIncrement()
    val carId = integer("car_id") references CarTable.id
    val userId = (integer("user_id") references OwnerTable.id).nullable()
    val rideId = integer("ride_id").nullable()
    val latitude = double("latitude")
    val longitude = double("longitude")
    val speed = double("speed").nullable()
    val timestamp = timestamp("timestamp")

    override val primaryKey = PrimaryKey(id)

    init {
        index(isUnique = false, carId, timestamp)
    }
}