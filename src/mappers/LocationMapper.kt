package mappers

import models.Location
import org.jetbrains.exposed.v1.core.ResultRow
import tables.LocationTable

fun ResultRow.toLocation(): Location = Location(
id = this[LocationTable.id],
carId = this[LocationTable.carId],
userId = this[LocationTable.userId],
rideId = this[LocationTable.rideId],
latitude = this[LocationTable.latitude],
longitude = this[LocationTable.longitude],
speed = this[LocationTable.speed],
timestamp = this[LocationTable.timestamp]
)