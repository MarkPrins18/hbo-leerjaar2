package repositories

import mappers.toRide
import models.Ride
import models.RideStatus
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.RideTable
import kotlin.time.Instant

class ExposedRideRepository : RideRepository {

    override suspend fun hasRideInProgress(reservationId: Int): Boolean = suspendTransaction {
        !RideTable.selectAll().where {
            (RideTable.reservationId eq reservationId) and (RideTable.status eq RideStatus.IN_PROGRESS)
        }.empty()
    }

    override suspend fun startRide(reservationId: Int, startTime: Instant): Ride = suspendTransaction {
        val inserted = RideTable.insert {
            it[RideTable.reservationId] = reservationId
            it[RideTable.startTime] = startTime
            it[RideTable.status] = RideStatus.IN_PROGRESS
        }
        val id = inserted[RideTable.id]
        RideTable.selectAll().where { RideTable.id eq id }.single().toRide()
    }

    override suspend fun getRideById(rideId: Int): Ride? = suspendTransaction {
        RideTable.selectAll().where { RideTable.id eq rideId }.singleOrNull()?.toRide()
    }

    override suspend fun finishRide(
        id: Int, endTime: Instant, distanceM: Double, avgAcceleration: Double, avgDeceleration: Double,
        bonusPoints: Int
    ): Ride? = suspendTransaction {
        RideTable.update({ RideTable.id eq id }) {
            it[RideTable.endTime] = endTime
            it[RideTable.status] = RideStatus.COMPLETED
            it[RideTable.distanceM] = distanceM
            it[RideTable.avgAcceleration] = avgAcceleration
            it[RideTable.avgDeceleration] = avgDeceleration
            it[RideTable.bonusPoints] = bonusPoints
        }
        RideTable.selectAll().where { RideTable.id eq id }.singleOrNull()?.toRide()
    }
}