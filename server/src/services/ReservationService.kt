package services

import models.ACTIVE_RESERVATION_STATUSES
import repositories.CarRepository
import repositories.ReservationRepository
import shared.models.Reservation
import shared.models.ReservationStatus
import shared.requests.ReservationRequest
import kotlin.time.Clock

class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val carRepository: CarRepository
) {
    suspend fun createReservation(userId: Int, request: ReservationRequest): Reservation {
        if (request.startTime <= Clock.System.now()) {
            throw ValidationException("Starttijd ligt in het verleden")
        }
        if (request.endTime <= request.startTime) {
            throw ValidationException("Eindtijd moet na de starttijd liggen")
        }
        val car = carRepository.getCarById(request.carId)
            ?: throw carNotFound(request.carId)

        if (car.userId == userId) {
            throw ForbiddenException("Je kunt je eigen auto niet reserveren")
        }
        if (!reservationRepository.userExists(userId)) {
            throw ResourceNotFoundException("Gebruiker", userId)
        }
        return reservationRepository.createReservationIfAvailable(
            request.carId, userId, request.startTime, request.endTime, ReservationStatus.CONFIRMED
        ) ?: throw ConflictException("Auto ${request.carId} is in dit tijdsblok al gereserveerd")
    }


    suspend fun cancelReservation(id: Int, userId: Int): Reservation {
        val reservation = reservationRepository.getReservationById(id)
            ?: throw ResourceNotFoundException("Reservering", id)

        if (reservation.userId != userId) {
            throw ForbiddenException()
        }

        if (reservation.status !in ACTIVE_RESERVATION_STATUSES) {
            throw ConflictException("Reservering $id is al ${reservation.status}")
        }
        if (reservation.startTime <= Clock.System.now()) {
            throw ConflictException("Starttijd van reservering $id is al verstreken")
        }
        return reservationRepository.updateStatus(id, ReservationStatus.CANCELLED)
            ?: throw ResourceNotFoundException("Reservering", id)
    }
}