package repositories

import mappers.toReservation
import models.Reservation
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
import tables.OwnerTable
import tables.ReservationTable
import kotlin.time.Instant

class ExposedReservationRepository : ReservationRepository {

    override suspend fun carExists(carId: Int): Boolean = suspendTransaction {
        !CarTable.selectAll().where { CarTable.id eq carId }.empty()
    }

    override suspend fun userExists(userId: Int): Boolean = suspendTransaction {
        !OwnerTable.selectAll().where { OwnerTable.id eq userId }.empty()
    }


    override suspend fun hasActiveOverlap(carId: Int, startTime: Instant, endTime: Instant): Boolean = suspendTransaction {
        !ReservationTable.selectAll().where {
            (ReservationTable.carId eq carId) and
                    (ReservationTable.status inList listOf(Reservation.PENDING, Reservation.CONFIRMED)) and
                    (ReservationTable.startTime less endTime) and
                    (ReservationTable.endTime greater startTime)
        }.empty()
    }


    override suspend fun createReservation(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: Reservation
    ): Reservation = suspendTransaction {
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

    override suspend fun updateStatus(id: Int, status: Reservation): Reservation? = suspendTransaction {
        ReservationTable.update({ ReservationTable.id eq id }) {
            it[ReservationTable.status] = status
        }
        ReservationTable.selectAll().where { ReservationTable.id eq id }.singleOrNull()?.toReservation()
    }
}