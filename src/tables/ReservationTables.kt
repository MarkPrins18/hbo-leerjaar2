package tables

import models.ReservationStatus
import org.jetbrains.exposed.v1.core.Table

// Issue #9 - Reservering-en-huurflow

object ReservationTable : Table("reservation") {
    val id = integer("id").autoIncrement()
    val carId = integer("car_id") references CarTable.id
    val renterId = integer("renter_id") references RenterTable.id
    val startTime = long("start_time")
    val endTime = long("end_time")
    val status = enumerationByName("status", 20, ReservationStatus::class)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

