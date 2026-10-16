package tables

import models.RideStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

val rideTables = listOf(RideTable)

object RideTable : Table("ride") {
    val id = integer("id").autoIncrement()
    val reservationId = integer("reservation_id") references ReservationTable.id
    val startTime = timestamp("start_time")
    val endTime = timestamp("end_time").nullable()
    val status = enumerationByName("status", 20, RideStatus::class)
    val distanceM = double("distance_m").nullable()
    val avgAcceleration = double("avg_acceleration").nullable()
    val avgDeceleration = double("avg_deceleration").nullable()
    val bonusPoints = integer("bonus_points").nullable()

    override val primaryKey = PrimaryKey(id)
}