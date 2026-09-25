package repositories

import kotlin.time.Instant
import models.Trip
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.TripTable

// Issue #11 - Ritregistratie
class ExposedTripRepository : TripRepository {

    override suspend fun getAllTrips(): List<Trip> = suspendTransaction {
        TripTable.selectAll().map { it.toTrip() }
    }

    override suspend fun getTripById(tripId: Int): Trip? = suspendTransaction {
        TripTable.selectAll().where { TripTable.id eq tripId }.map { it.toTrip() }.singleOrNull()
    }

    override suspend fun getTripsForReservation(reservationId: Int): List<Trip> = suspendTransaction {
        TripTable.selectAll().where { TripTable.reservationId eq reservationId }.map { it.toTrip() }
    }

    override suspend fun startTrip(trip: Trip): Trip = suspendTransaction {
        val newId = TripTable.insert {
            it[reservationId] = trip.reservationId
            it[startTime] = trip.startTime.toEpochMilliseconds()
            it[endTime] = null
            it[startLatitude] = trip.startLatitude
            it[startLongitude] = trip.startLongitude
            it[endLatitude] = null
            it[endLongitude] = null
            it[distanceKm] = null
        }[TripTable.id]

        TripTable.selectAll().where { TripTable.id eq newId }.map { it.toTrip() }.single()
    }

    override suspend fun endTrip(
        tripId: Int,
        endTime: Instant,
        endLatitude: Double,
        endLongitude: Double,
        distanceKm: Double
    ): Trip? = suspendTransaction {
        val updated = TripTable.update({ TripTable.id eq tripId }) {
            it[TripTable.endTime] = endTime.toEpochMilliseconds()
            it[TripTable.endLatitude] = endLatitude
            it[TripTable.endLongitude] = endLongitude
            it[TripTable.distanceKm] = distanceKm
        }
        if (updated == 0) return@suspendTransaction null

        TripTable.selectAll().where { TripTable.id eq tripId }.map { it.toTrip() }.singleOrNull()
    }

    private fun ResultRow.toTrip(): Trip = Trip(
        id = this[TripTable.id],
        reservationId = this[TripTable.reservationId],
        startTime = Instant.fromEpochMilliseconds(this[TripTable.startTime]),
        endTime = this[TripTable.endTime]?.let { Instant.fromEpochMilliseconds(it) },
        startLatitude = this[TripTable.startLatitude],
        startLongitude = this[TripTable.startLongitude],
        endLatitude = this[TripTable.endLatitude],
        endLongitude = this[TripTable.endLongitude],
        distanceKm = this[TripTable.distanceKm]
    )
}
