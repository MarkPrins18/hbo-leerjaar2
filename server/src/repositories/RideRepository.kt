package repositories

import shared.models.Ride
import shared.requests.RideFinishRequest
import kotlin.time.Instant

interface RideRepository {
    /** Locks the reservation, checks for a ride in progress and inserts in one transaction. Returns null if one is already running. */
    suspend fun startRideIfNoneInProgress(reservationId: Int, startTime: Instant): Ride?
    suspend fun getRideById(rideId: Int): Ride?
    suspend fun getRideInProgressByUserId(userId: Int): Ride?

    /**
     * Only finishes a ride that is still in progress and stores the car's new location from [request],
     * in one transaction. Returns null if no such ride was updated.
     */
    suspend fun finishRide(id: Int, endTime: Instant, request: RideFinishRequest, bonusPoints: Int): Ride?
}
