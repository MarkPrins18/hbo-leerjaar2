package services

import io.ktor.http.HttpStatusCode
import models.ReservationStatus
import models.Ride
import models.RideStatus
import repositories.ReservationRepository
import repositories.RideRepository
import dto.RideFinishRequest
import kotlin.time.Clock

class RideService(
    private val rideRepository: RideRepository,
    private val reservationRepository: ReservationRepository
) {
    suspend fun startRide(reservationId: Int): Ride {
        val reservation = reservationRepository.getReservationById(reservationId)
            ?: throw ApiException(HttpStatusCode.NotFound, "Reservering $reservationId niet gevonden")

        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw ApiException(HttpStatusCode.Conflict, "Reservering $reservationId is ${reservation.status}")
        }
        val now = Clock.System.now()
        if (now < reservation.startTime || now >= reservation.endTime) {
            throw ApiException(HttpStatusCode.Conflict, "Reservering $reservationId is nu niet actief")
        }
        if (rideRepository.hasRideInProgress(reservationId)) {
            throw ApiException(HttpStatusCode.Conflict, "Er loopt al een rit voor reservering $reservationId")
        }
        return rideRepository.startRide(reservationId, now)
    }

    suspend fun finishRide(rideId: Int, request: RideFinishRequest): Ride {
        if (request.distanceM < 0 || request.avgAcceleration < 0 || request.avgDeceleration < 0) {
            throw ApiException(HttpStatusCode.BadRequest, "Afstand en acceleraties mogen niet negatief zijn")
        }
        val ride = rideRepository.getRideById(rideId)
            ?: throw ApiException(HttpStatusCode.NotFound, "Rit $rideId niet gevonden")

        if (ride.status != RideStatus.IN_PROGRESS) {
            throw ApiException(HttpStatusCode.Conflict, "Rit $rideId is al afgerond")
        }
        val bonusPoints = calculateBonusPoints(request.distanceM, request.avgAcceleration, request.avgDeceleration)
        return rideRepository.finishRide(
            rideId, Clock.System.now(), request.distanceM, request.avgAcceleration, request.avgDeceleration, bonusPoints
        ) ?: throw ApiException(HttpStatusCode.NotFound, "Rit $rideId niet gevonden")
    }
}