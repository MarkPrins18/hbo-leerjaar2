package repositories

import mappers.toRide
import shared.models.Ride
import shared.models.RideStatus
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.ReservationTable
import tables.RideTable
import kotlin.time.Instant

class ExposedRideRepository : RideRepository {

    override suspend fun startRideIfNoneInProgress(reservationId: Int, startTime: Instant): Ride? = suspendTransaction {
        ReservationTable.selectAll().where { ReservationTable.id eq reservationId }.forUpdate().toList()

        val hasRideInProgress = !RideTable.selectAll().where {
            (RideTable.reservationId eq reservationId) and (RideTable.status eq RideStatus.IN_PROGRESS)
        }.empty()
        if (hasRideInProgress) return@suspendTransaction null

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

    override suspend fun getRideInProgressByUserId(userId: Int): Ride? = suspendTransaction {
        RideTable.innerJoin(ReservationTable, { RideTable.reservationId }, { ReservationTable.id })
            .selectAll()
            .where { (ReservationTable.userId eq userId) and (RideTable.status eq RideStatus.IN_PROGRESS) }
            .orderBy(RideTable.startTime, SortOrder.DESC)
            .firstOrNull()
            ?.toRide()
    }

    override suspend fun finishRide(
        id: Int, endTime: Instant, distanceM: Double, avgAcceleration: Double, avgDeceleration: Double,
        bonusPoints: Int
    ): Ride? = suspendTransaction {
        val updated = RideTable.update({ (RideTable.id eq id) and (RideTable.status eq RideStatus.IN_PROGRESS) }) {
            it[RideTable.endTime] = endTime
            it[RideTable.status] = RideStatus.COMPLETED
            it[RideTable.distanceM] = distanceM
            it[RideTable.avgAcceleration] = avgAcceleration
            it[RideTable.avgDeceleration] = avgDeceleration
            it[RideTable.bonusPoints] = bonusPoints
        }
        if (updated == 0) return@suspendTransaction null
        RideTable.selectAll().where { RideTable.id eq id }.single().toRide()
    }
}
