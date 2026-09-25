package repositories

import kotlin.time.Instant
import models.Trip

// Issue #11 - Ritregistratie

interface TripRepository {
    suspend fun getAllTrips(): List<Trip>

    suspend fun getTripById(tripId: Int): Trip?

    suspend fun getTripsForReservation(reservationId: Int): List<Trip>

    /** Start een nieuwe rit. Het `id`-veld van [trip] wordt genegeerd. */
    suspend fun startTrip(trip: Trip): Trip

    /** Rondt een lopende rit af. Geeft null terug als de rit niet bestaat. */
    suspend fun endTrip(
        tripId: Int,
        endTime: Instant,
        endLatitude: Double,
        endLongitude: Double,
        distanceKm: Double
    ): Trip?
}
