package repositories

import mappers.toReservation
import models.ACTIVE_RESERVATION_STATUSES
import shared.models.Reservation
import shared.models.ReservationStatus
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.CarTable
import tables.UserTable
import tables.ReservationTable
import kotlin.time.Instant

class ExposedReservationRepository : ReservationRepository {

    override suspend fun userExists(userId: Int): Boolean = suspendTransaction {
        !UserTable.selectAll().where { UserTable.id eq userId }.empty()
    }

    override suspend fun createReservationIfAvailable(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus
    ): Reservation? = suspendTransaction {
        CarTable.selectAll().where { CarTable.id eq carId }.forUpdate().toList()

        val hasOverlap = !ReservationTable.selectAll().where {
            (ReservationTable.carId eq carId) and
                    (ReservationTable.status inList ACTIVE_RESERVATION_STATUSES) and
                    (ReservationTable.startTime less endTime) and
                    (ReservationTable.endTime greater startTime)
        }.empty()
        if (hasOverlap) return@suspendTransaction null

        val inserted = ReservationTable.insert {
            it[ReservationTable.carId] = carId
            it[ReservationTable.userId] = userId
            it[ReservationTable.startTime] = startTime
            it[ReservationTable.endTime] = endTime
            it[ReservationTable.status] = status
        }
        val id = inserted[ReservationTable.id]
        ReservationTable.selectAll().where { ReservationTable.id eq id }.single().toReservation()
    }


    override suspend fun getReservationById(id: Int): Reservation? = suspendTransaction {
        ReservationTable.selectAll().where { ReservationTable.id eq id }.singleOrNull()?.toReservation()
    }

    override suspend fun getReservationsByCarAndUser(carId: Int, userId: Int): List<Reservation> = suspendTransaction {
        ReservationTable.selectAll()
            .where { (ReservationTable.carId eq carId) and (ReservationTable.userId eq userId) }
            .map { it.toReservation() }
    }

    override suspend fun getReservationsByUserId(userId: Int): List<Reservation> = suspendTransaction {
        ReservationTable.selectAll()
            .where { ReservationTable.userId eq userId }
            .map { it.toReservation() }
    }

    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? = suspendTransaction {
        ReservationTable.update({ ReservationTable.id eq id }) {
            it[ReservationTable.status] = status
        }
        ReservationTable.selectAll().where { ReservationTable.id eq id }.singleOrNull()?.toReservation()
    }
}