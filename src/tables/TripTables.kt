package tables

import org.jetbrains.exposed.v1.core.Table

// Issue #11 - Ritregistratie

object TripTable : Table("trip") {
    val id = integer("id").autoIncrement()
    val reservationId = integer("reservation_id") references ReservationTable.id
    val startTime = long("start_time")
    val endTime = long("end_time").nullable()
    val startLatitude = double("start_latitude")
    val startLongitude = double("start_longitude")
    val endLatitude = double("end_latitude").nullable()
    val endLongitude = double("end_longitude").nullable()
    val distanceKm = double("distance_km").nullable()

    override val primaryKey = PrimaryKey(id)
}

