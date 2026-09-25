package repositories

import models.Location

interface LocationRepository {
    suspend fun getLocationsForCar(carId: Int): List<Location>

    suspend fun getLatestLocationForCar(carId: Int): Location?

    /** Slaat een nieuwe locatiemelding op. Het `id`-veld van [location] wordt genegeerd. */
    suspend fun recordLocation(location: Location): Location
}
