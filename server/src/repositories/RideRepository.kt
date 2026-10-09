package repositories

import models.Ride
import kotlin.time.Instant

interface RideRepository {
    suspend fun hasRideInProgress(reservationId: Int): Boolean
    suspend fun startRide(reservationId: Int, startTime: Instant): Ride
    suspend fun getRideById(rideId: Int): Ride?
    suspend fun finishRide(id: Int, endTime: Instant, distanceM: Double, avgAcceleration: Double, avgDeceleration: Double, bonusPoints: Int): Ride?
}