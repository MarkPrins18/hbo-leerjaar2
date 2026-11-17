package repositories

import models.Location
import kotlin.time.Instant

interface LocationRepository {
    suspend fun getLatestLocationByCarId(carId: Int): Location?

    /** The latest location of every car that has one, in one query; same order as [getLatestLocationByCarId]. */
    suspend fun getLatestLocationsByCarId(): Map<Int, Location>

    /** Adds a new row, which becomes the car's current location. */
    suspend fun addLocation(carId: Int, latitude: Double, longitude: Double, timestamp: Instant): Location
}
