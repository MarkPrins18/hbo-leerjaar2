package services

import models.Reservation
import models.ReservationStatus
import repositories.ReservationRepository
import requests.ReservationRequest
import kotlin.time.Clock

class ReservationService(
    private val reservationRepository: ReservationRepository
) {
    suspend fun createReservation(request: ReservationRequest): Reservation {
        if (request.startTime <= Clock.System.now()) {
            throw ValidationException("Starttijd ligt in het verleden")
        }
        if (request.endTime <= request.startTime) {
            throw ValidationException("Eindtijd moet na de starttijd liggen")
        }
        if (!reservationRepository.carExists(request.carId)) {
            throw ResourceNotFoundException("Auto ${request.carId} niet gevonden")
        }
        if (!reservationRepository.userExists(request.userId)) {
            throw ResourceNotFoundException("Gebruiker ${request.userId} niet gevonden")
        }
        if (reservationRepository.hasActiveOverlap(request.carId, request.startTime, request.endTime)) {
            throw ConflictException("Auto ${request.carId} is in dit tijdsblok al gereserveerd")
        }
        return reservationRepository.createReservation(
            request.carId, request.userId, request.startTime, request.endTime, ReservationStatus.CONFIRMED
        )
    }


    suspend fun cancelReservation(id: Int): Reservation {
        val reservation = reservationRepository.getReservationById(id)
            ?: throw ResourceNotFoundException("Reservering $id niet gevonden")

        if (reservation.status != ReservationStatus.PENDING && reservation.status != ReservationStatus.CONFIRMED) {
            throw ConflictException("Reservering $id is al ${reservation.status}")
        }
        if (reservation.startTime <= Clock.System.now()) {
            throw ConflictException("Starttijd van reservering $id is al verstreken")
        }
        return reservationRepository.updateStatus(id, ReservationStatus.CANCELLED)
            ?: throw ResourceNotFoundException("Reservering $id niet gevonden")
    }
}