package tables

import models.ReservationStatus
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.inList

val reservationTables = listOf(ReservationTable)

object ReservationTable : Table("reservation") {
    val id = integer("id").autoIncrement()
    val carId = integer("car_id") references CarTable.id
    val userId = integer("user_id") references OwnerTable.id
    val startTime = timestamp("start_time")
    val endTime = timestamp("end_time")
    val status = enumerationByName("status", 20, ReservationStatus::class)

    override val primaryKey = PrimaryKey(id)

    init {
        check("chk_reservation_time") { startTime less endTime }
        check("chk_reservation_status") { status inList ReservationStatus.entries }
    }
}