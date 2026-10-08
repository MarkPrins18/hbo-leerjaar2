package mappers

import models.Reservation
import org.jetbrains.exposed.v1.core.ResultRow
import tables.ReservationTable

fun ResultRow.toReservation(): Reservation = Reservation(
    id = this[ReservationTable.id],
    carId = this[ReservationTable.carId],
    userId = this[ReservationTable.userId],
    startTime = this[ReservationTable.startTime],
    endTime = this[ReservationTable.endTime],
    status = this[ReservationTable.status]
)