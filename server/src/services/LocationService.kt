package services

import models.Reservation
import models.ReservationStatus
import repositories.CarRepository
import repositories.LocationRepository
import repositories.ReservationRepository
import kotlin.time.Clock
import kotlin.time.Instant

class LocationService(
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository,
    private val reservationRepository: ReservationRepository
) {
    /** Only the owner and a renter with a confirmed reservation that is active right now may see the location. */
    suspend fun getMapsUrl(carId: Int, userId: Int): String {
        val car = carRepository.getCarById(carId)
            ?: throw ResourceNotFoundException("Auto $carId niet gevonden")

        if (car.userId != userId && !hasActiveReservation(carId, userId)) throw ForbiddenException()

        val location = locationRepository.getLatestLocationByCarId(carId)
            ?: throw ResourceNotFoundException("Geen locatie bekend voor deze auto")

        return "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"
    }

    private suspend fun hasActiveReservation(carId: Int, userId: Int): Boolean {
        val now = Clock.System.now()
        return reservationRepository.getReservationsByCarAndUser(carId, userId).any { it.isActiveAt(now) }
    }

    private fun Reservation.isActiveAt(now: Instant): Boolean =
        status == ReservationStatus.CONFIRMED && now >= startTime && now < endTime
}
