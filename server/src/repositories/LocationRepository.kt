package repositories

import models.Location

interface LocationRepository {
    suspend fun getLatestLocationByCarId(carId: Int): Location?
}