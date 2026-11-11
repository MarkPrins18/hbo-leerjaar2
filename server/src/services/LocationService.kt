package services

import mappers.toResponse
import shared.models.Reservation
import shared.models.ReservationStatus
import repositories.CarRepository
import repositories.LocationRepository
import repositories.ReservationRepository
import shared.requests.LocationRequest
import shared.responses.LocationResponse
import kotlin.time.Clock
import kotlin.time.Instant

class LocationService(
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository,
    private val reservationRepository: ReservationRepository
) {
    suspend fun getMapsUrl(carId: Int, userId: Int): String {
        val car = carRepository.getCarById(carId)
            ?: throw ResourceNotFoundException("Auto", carId)

        if (car.userId != userId && !hasActiveReservation(carId, userId)) throw ForbiddenException()

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

    private suspend fun hasActiveReservation(carId: Int, userId: Int): Boolean {
        val now = Clock.System.now()
        return reservationRepository.getReservationsByCarAndUser(carId, userId).any { it.isActiveAt(now) }
    }

    private fun Reservation.isActiveAt(now: Instant): Boolean =
        status == ReservationStatus.CONFIRMED && now >= startTime && now < endTime
}
