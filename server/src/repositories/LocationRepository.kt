package repositories

import models.Location
import kotlin.time.Instant

interface LocationRepository {
    suspend fun getLatestLocationByCarId(carId: Int): Location?

    /** Adds a new row, which becomes the car's current location. */
    suspend fun addLocation(carId: Int, latitude: Double, longitude: Double, timestamp: Instant): Location
}
