package mappers

import models.Ride
import org.jetbrains.exposed.v1.core.ResultRow
import tables.RideTable

fun ResultRow.toRide(): Ride = Ride(
    id = this[RideTable.id],
    reservationId = this[RideTable.reservationId],
    startTime = this[RideTable.startTime],
    endTime = this[RideTable.endTime],
    status = this[RideTable.status],
    distanceM = this[RideTable.distanceM],
    avgAcceleration = this[RideTable.avgAcceleration],
    avgDeceleration = this[RideTable.avgDeceleration],
    bonusPoints = this[RideTable.bonusPoints],
)