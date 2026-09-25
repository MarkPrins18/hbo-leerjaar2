package repositories

import kotlin.time.Instant
import models.Location
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.LocationTable

class ExposedLocationRepository : LocationRepository {

    override suspend fun getLocationsForCar(carId: Int): List<Location> = suspendTransaction {
        LocationTable.selectAll()
            .where { LocationTable.carId eq carId }
            .orderBy(LocationTable.timestamp)
            .map { it.toLocation() }
    }

    override suspend fun getLatestLocationForCar(carId: Int): Location? = suspendTransaction {
        LocationTable.selectAll()
            .where { LocationTable.carId eq carId }
            .orderBy(LocationTable.timestamp, SortOrder.DESC)
            .limit(1)
            .map { it.toLocation() }
            .singleOrNull()
    }

    override suspend fun recordLocation(location: Location): Location = suspendTransaction {
        val newId = LocationTable.insert {
            it[carId] = location.carId
            it[userId] = location.userId
            it[latitude] = location.latitude
            it[longitude] = location.longitude
            it[timestamp] = location.timestamp.toEpochMilliseconds()
        }[LocationTable.id]

        location.copy(id = newId)
    }

    private fun ResultRow.toLocation(): Location = Location(
        id = this[LocationTable.id],
        carId = this[LocationTable.carId],
        userId = this[LocationTable.userId],
        latitude = this[LocationTable.latitude],
        longitude = this[LocationTable.longitude],
        timestamp = Instant.fromEpochMilliseconds(this[LocationTable.timestamp])
    )
}
