package repositories

import contracts.models.Ride
import contracts.requests.RideFinishRequest
import kotlin.time.Instant

interface RideRepository {
    /**
     * Locks the car and the reservation, checks that the reservation is still CONFIRMED and that the car has no
     * ride in progress on any reservation, and inserts in one transaction. Returns null if the car is already
     * being driven or the reservation is no longer confirmed.
     */
    suspend fun startRideIfNoneInProgress(reservationId: Int, startTime: Instant): Ride?
    suspend fun getRideById(rideId: Int): Ride?
    suspend fun getRideInProgressByUserId(userId: Int): Ride?

    /** All rides on the user's reservations, newest first. */
    suspend fun getRidesByUserId(userId: Int): List<Ride>

    /** Sum of the bonus points of the user's completed rides; 0 without rides. */
    suspend fun getTotalBonusPointsByUserId(userId: Int): Int

    /**
     * Only finishes a ride that is still in progress, sets its reservation to COMPLETED and stores
     * the car's new location from [request], all in one transaction. Returns null if no such ride was updated.
     */
    suspend fun finishRide(id: Int, endTime: Instant, request: RideFinishRequest, bonusPoints: Int): Ride?
}
