package services

import mappers.toResponse
import repositories.CarRepository
import repositories.LocationRepository
import contracts.requests.LocationRequest
import contracts.responses.LocationResponse
import kotlin.time.Clock

class LocationService(
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository
) {
    /** Any logged-in user may get the route link; the position itself is public via GET /cars/map. */
    suspend fun getMapsUrl(carId: Int): String {
        carRepository.getCarById(carId) ?: throw carNotFound(carId)

        val location = locationRepository.getLatestLocationByCarId(carId)
            ?: throw ResourceNotFoundException("Geen locatie bekend voor deze auto")

        return "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
    }

    suspend fun setLocation(carId: Int, userId: Int, request: LocationRequest): LocationResponse {
        carRepository.getOwnedCar(carId, userId)

        request.validate()

        return locationRepository.addLocation(carId, request.latitude, request.longitude, Clock.System.now())
            .toResponse()
    }
}
