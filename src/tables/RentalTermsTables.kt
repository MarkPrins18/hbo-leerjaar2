package tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.time

val rentalTermsTables = listOf(RentalTermsTable)

object RentalTermsTable : Table("rental_terms") {
    val id = integer("id").autoIncrement()
    val carId = (integer("car_id") references CarTable.id).uniqueIndex()
    val pricePerDay = decimal("price_per_day", 8, 2)
    val pickupLocation = varchar("pickup_location", 255)
    val pickupTimeStart = time("pickup_time_start").nullable()
    val pickupTimeEnd = time("pickup_time_end").nullable()
    val returnLocation = varchar("return_location", 255).nullable()
    val returnTimeStart = time("return_time_start").nullable()
    val returnTimeEnd = time("return_time_end").nullable()

    override val primaryKey = PrimaryKey(id)
}
