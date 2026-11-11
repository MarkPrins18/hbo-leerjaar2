package services

import shared.models.ReservationStatus
import shared.models.Ride
import shared.models.RideStatus
import repositories.ReservationRepository
import repositories.RideRepository
import shared.dto.RideFinishRequest
import kotlin.time.Clock

internal const val MAX_RIDE_DISTANCE_M = 1_000_000.0

class RideService(
    private val rideRepository: RideRepository,
    private val reservationRepository: ReservationRepository
) {
    suspend fun startRide(reservationId: Int, userId: Int): Ride {
        val reservation = reservationRepository.getReservationById(reservationId)
            ?: throw ResourceNotFoundException("Reservering", reservationId)

        if (reservation.userId != userId) {
            throw ForbiddenException()
        }
        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw ConflictException("Reservering $reservationId is ${reservation.status}")
        }
        val now = Clock.System.now()
        if (now < reservation.startTime || now >= reservation.endTime) {
            throw ConflictException("Reservering $reservationId is nu niet actief")
        }
        return rideRepository.startRideIfNoneInProgress(reservationId, now)
            ?: throw ConflictException("Er loopt al een rit voor reservering $reservationId")
    }

    suspend fun getActiveRide(userId: Int): Ride =
        rideRepository.getRideInProgressByUserId(userId)
            ?: throw ResourceNotFoundException("Geen lopende rit gevonden")

    suspend fun finishRide(rideId: Int, userId: Int, request: RideFinishRequest): Ride {
        if (request.distanceM < 0 || request.avgAcceleration < 0 || request.avgDeceleration < 0) {
            throw ValidationException("Afstand en acceleraties mogen niet negatief zijn")
        }
        if (request.distanceM > MAX_RIDE_DISTANCE_M) {
            throw ValidationException("Afstand mag niet groter zijn dan ${(MAX_RIDE_DISTANCE_M / 1000).toInt()} km per rit")
        }
        val ride = rideRepository.getRideById(rideId)
            ?: throw ResourceNotFoundException("Rit", rideId)
        val reservation = reservationRepository.getReservationById(ride.reservationId)
            ?: throw ResourceNotFoundException("Reservering", ride.reservationId)

        if (reservation.userId != userId) {
            throw ForbiddenException()
        }

        if (ride.status != RideStatus.IN_PROGRESS) {
            throw rideAlreadyFinished(rideId)
        }
        val bonusPoints = calculateBonusPoints(request.distanceM, request.avgAcceleration, request.avgDeceleration)
        return rideRepository.finishRide(
            rideId, Clock.System.now(), request.distanceM, request.avgAcceleration, request.avgDeceleration, bonusPoints
        ) ?: throw rideAlreadyFinished(rideId)
    }

    private fun rideAlreadyFinished(rideId: Int) = ConflictException("Rit $rideId is al afgerond")
}
