package services

import repositories.LocationRepository

class LocationService(
    private val locationRepository: LocationRepository
) {
    suspend fun getMapsUrl(carId: Int): String {
        val location = locationRepository.getLatestLocationByCarId(carId)
            ?: throw ResourceNotFoundException("Geen locatie bekend voor auto $carId")

        return "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
    }
}