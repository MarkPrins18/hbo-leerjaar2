package services

import models.ACTIVE_RESERVATION_STATUSES
import models.Reservation
import models.ReservationStatus
import repositories.ReservationRepository
import requests.ReservationRequest
import kotlin.time.Clock

class ReservationService(
    private val reservationRepository: ReservationRepository
) {
    suspend fun createReservation(userId: Int, request: ReservationRequest): Reservation {
        if (request.startTime <= Clock.System.now()) {
            throw ValidationException("Starttijd ligt in het verleden")
        }
        if (request.endTime <= request.startTime) {
            throw ValidationException("Eindtijd moet na de starttijd liggen")
        }
        if (!reservationRepository.carExists(request.carId)) {
            throw ResourceNotFoundException("Auto", request.carId)
        }
        if (!reservationRepository.userExists(userId)) {
            throw ResourceNotFoundException("Gebruiker", userId)
        }
        if (reservationRepository.hasActiveOverlap(request.carId, request.startTime, request.endTime)) {
            throw ConflictException("Auto ${request.carId} is in dit tijdsblok al gereserveerd")
        }
        return reservationRepository.createReservation(
            request.carId, userId, request.startTime, request.endTime, ReservationStatus.CONFIRMED
        )
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