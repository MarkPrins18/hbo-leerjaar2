package repositories

import mappers.toRide
import shared.models.Ride
import shared.models.ReservationStatus
import shared.models.RideStatus

import shared.requests.RideFinishRequest
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.core.sum

import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.ReservationTable
import tables.RideTable
import kotlin.time.Instant

class ExposedRideRepository : RideRepository {

    override suspend fun startRideIfNoneInProgress(reservationId: Int, startTime: Instant): Ride? = suspendTransaction {
        val reservation = ReservationTable.selectAll().where { ReservationTable.id eq reservationId }.forUpdate().singleOrNull()
        // Checked again under the lock: a finish in between may have completed the reservation
        if (reservation?.get(ReservationTable.status) != ReservationStatus.CONFIRMED) return@suspendTransaction null

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

    override suspend fun getRidesByUserId(userId: Int): List<Ride> = suspendTransaction {
        RideTable.innerJoin(ReservationTable, { RideTable.reservationId }, { ReservationTable.id })
            .selectAll()
            .where { ReservationTable.userId eq userId }
            .orderBy(RideTable.startTime to SortOrder.DESC, RideTable.id to SortOrder.DESC)
            .map { it.toRide() }
    }

    override suspend fun getTotalBonusPointsByUserId(userId: Int): Int = suspendTransaction {
        val total = RideTable.bonusPoints.sum()
        RideTable.innerJoin(ReservationTable, { RideTable.reservationId }, { ReservationTable.id })
            .select(total)
            .where { (ReservationTable.userId eq userId) and (RideTable.status eq RideStatus.COMPLETED) }
            .single()[total] ?: 0
    }

    override suspend fun finishRide(

        id: Int, endTime: Instant, request: RideFinishRequest, bonusPoints: Int
    ): Ride? = suspendTransaction {
        val updated = RideTable.update({ (RideTable.id eq id) and (RideTable.status eq RideStatus.IN_PROGRESS) }) {
            it[RideTable.endTime] = endTime
            it[RideTable.status] = RideStatus.COMPLETED
            it[RideTable.distanceM] = request.distanceM
            it[RideTable.avgAcceleration] = request.avgAcceleration
            it[RideTable.avgDeceleration] = request.avgDeceleration
            it[RideTable.bonusPoints] = bonusPoints
        }
        if (updated == 0) return@suspendTransaction null
        val ride = RideTable.selectAll().where { RideTable.id eq id }.single().toRide()

        val carId = ReservationTable.select(ReservationTable.carId)
            .where { ReservationTable.id eq ride.reservationId }
            .single()[ReservationTable.carId]
        ReservationTable.update({ ReservationTable.id eq ride.reservationId }) {
            it[ReservationTable.status] = ReservationStatus.COMPLETED
        }
        insertParkedLocation(carId, request.latitude, request.longitude, endTime)
        ride
    }
}
