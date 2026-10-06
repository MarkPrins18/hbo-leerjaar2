package services

import io.ktor.http.HttpStatusCode
import models.Reservation
import repositories.ReservationRepository
import requests.ReservationRequest
import kotlin.time.Clock

class ReservationService(
    private val reservationRepository: ReservationRepository
) {
    suspend fun createReservation(request: ReservationRequest): Reservation {
        if (request.startTime <= Clock.System.now()) {
            throw ApiException(HttpStatusCode.BadRequest, "Starttijd ligt in het verleden")
        }
        if (request.endTime <= request.startTime) {
            throw ApiException(HttpStatusCode.BadRequest, "Eindtijd moet na de starttijd liggen")
        }
        if (!reservationRepository.carExists(request.carId)) {
            throw ApiException(HttpStatusCode.NotFound, "Auto ${request.carId} niet gevonden")
        }
        if (!reservationRepository.userExists(request.userId)) {
            throw ApiException(HttpStatusCode.NotFound, "Gebruiker ${request.userId} niet gevonden")
        }
        if (reservationRepository.hasActiveOverlap(request.carId, request.startTime, request.endTime)) {
            throw ApiException(HttpStatusCode.Conflict, "Auto ${request.carId} is in dit tijdsblok al gereserveerd")
        }
        return reservationRepository.createReservation(
            request.carId, request.userId, request.startTime, request.endTime, ReservationStatus.CONFIRMED
        )
    }


    suspend fun cancelReservation(id: Int): Reservation {
        val reservation = reservationRepository.getReservationById(id)
            ?: throw ApiException(HttpStatusCode.NotFound, "Reservering $id niet gevonden")

        if (reservation.status != ReservationStatus.PENDING && reservation.status != ReservationStatus.CONFIRMED) {
            throw ApiException(HttpStatusCode.Conflict, "Reservering $id is al ${reservation.status}")
        }
        if (reservation.startTime <= Clock.System.now()) {
            throw ApiException(HttpStatusCode.Conflict, "Starttijd van reservering $id is al verstreken")
        }
        return reservationRepository.updateStatus(id, ReservationStatus.CANCELLED)
            ?: throw ApiException(HttpStatusCode.NotFound, "Reservering $id niet gevonden")
    }
}