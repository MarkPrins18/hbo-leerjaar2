package repositories

import models.Location
import mappers.toLocation
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.LocationTable
import kotlin.time.Instant

class ExposedLocationRepository : LocationRepository {
    override suspend fun getLatestLocationByCarId(carId: Int): Location? = suspendTransaction {
        LocationTable
            .selectAll()
            .where { LocationTable.carId eq carId }
            .orderBy(LocationTable.timestamp to SortOrder.DESC, LocationTable.id to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.toLocation()
    }

    override suspend fun addLocation(carId: Int, latitude: Double, longitude: Double, timestamp: Instant): Location =
        suspendTransaction {
            insertParkedLocation(carId, latitude, longitude, timestamp)
        }
}

/**
 * Inserts the position of a parked car, without driver or ride. Must run inside a transaction,
 * so callers can combine it with other changes. A parked car stands still, so speed is 0.
 */
internal fun insertParkedLocation(carId: Int, latitude: Double, longitude: Double, timestamp: Instant): Location {
    val inserted = LocationTable.insert {
        it[LocationTable.carId] = carId
        it[LocationTable.latitude] = latitude
        it[LocationTable.longitude] = longitude
        it[LocationTable.speed] = 0.0
        it[LocationTable.timestamp] = timestamp
    }
    val id = inserted[LocationTable.id]
    return LocationTable.selectAll().where { LocationTable.id eq id }.single().toLocation()
}
