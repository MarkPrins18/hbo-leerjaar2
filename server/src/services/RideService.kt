package services

import contracts.models.ReservationStatus
import contracts.models.Ride
import contracts.models.RideStatus
import repositories.ReservationRepository
import repositories.RideRepository
import contracts.requests.RideFinishRequest
import kotlin.time.Clock

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
            ?: throw ConflictException("Er loopt al een rit met auto ${reservation.carId} of reservering $reservationId is niet meer bevestigd")
    }

    suspend fun getActiveRide(userId: Int): Ride? =
        rideRepository.getRideInProgressByUserId(userId)

    suspend fun getRides(userId: Int): List<Ride> =
        rideRepository.getRidesByUserId(userId)

    suspend fun finishRide(rideId: Int, userId: Int, request: RideFinishRequest): Ride {
        val ride = rideRepository.getRideById(rideId)
            ?: throw ResourceNotFoundException("Rit", rideId)
        val reservation = reservationRepository.getReservationById(ride.reservationId)
            ?: throw ResourceNotFoundException("Reservering", ride.reservationId)

        if (reservation.userId != userId) {
            throw ForbiddenException()
        }
        request.validate()

        if (ride.status != RideStatus.IN_PROGRESS) {
            throw rideAlreadyFinished(rideId)
        }
        val bonusPoints = calculateBonusPoints(request.distanceM, request.avgAcceleration, request.avgDeceleration)
        return rideRepository.finishRide(rideId, Clock.System.now(), request, bonusPoints)
            ?: throw rideAlreadyFinished(rideId)
    }

    private fun rideAlreadyFinished(rideId: Int) = ConflictException("Rit $rideId is al afgerond")
}
