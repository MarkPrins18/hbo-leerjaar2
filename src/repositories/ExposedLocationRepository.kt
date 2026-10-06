package repositories

import models.Location
import mappers.toLocation
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.LocationTable

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
}