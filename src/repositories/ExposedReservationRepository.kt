package repositories

import kotlin.time.Instant
import models.Reservation
import models.ReservationStatus
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.ReservationTable

// Issue #9 - Reservering-en-huurflow
//
// LET OP - kon niet gecompileerd worden in deze omgeving (geen toegang tot Maven
// Central hier): .selectAll().where { ... } is het moderne Exposed 1.x-idioom
// (opvolger van het oudere Table.select { ... }). Mocht jouw Exposed-versie dit
// om wat voor reden dan ook niet accepteren, vervang `.selectAll().where { ... }`
// door `.select { ... }`.
class ExposedReservationRepository : ReservationRepository {

    override suspend fun getAllReservations(): List<Reservation> = suspendTransaction {
        ReservationTable.selectAll().map { it.toReservation() }
    }

    override suspend fun getReservationById(reservationId: Int): Reservation? = suspendTransaction {
        ReservationTable.selectAll()
            .where { ReservationTable.id eq reservationId }
            .map { it.toReservation() }
            .singleOrNull()
    }

    override suspend fun getReservationsForRenter(renterId: Int): List<Reservation> = suspendTransaction {
        ReservationTable.selectAll()
            .where { ReservationTable.renterId eq renterId }
            .map { it.toReservation() }
    }

    override suspend fun createReservation(reservation: Reservation): Reservation? = suspendTransaction {
        val overlapping = ReservationTable.selectAll()
            .where {
                (ReservationTable.carId eq reservation.carId) and
                        (ReservationTable.status inList listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)) and
                        (ReservationTable.startTime less reservation.endTime.toEpochMilliseconds()) and
                        (ReservationTable.endTime greater reservation.startTime.toEpochMilliseconds())
            }
            .any()

        if (overlapping) return@suspendTransaction null

        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val newId = ReservationTable.insert {
            it[carId] = reservation.carId
            it[renterId] = reservation.renterId
            it[startTime] = reservation.startTime.toEpochMilliseconds()
            it[endTime] = reservation.endTime.toEpochMilliseconds()
            it[status] = ReservationStatus.PENDING
            it[createdAt] = now.toEpochMilliseconds()
        }[ReservationTable.id]

        reservation.copy(id = newId, status = ReservationStatus.PENDING, createdAt = now)
    }

    override suspend fun updateReservationStatus(reservationId: Int, status: ReservationStatus): Reservation? =
        suspendTransaction {
            val updated = ReservationTable.update({ ReservationTable.id eq reservationId }) {
                it[ReservationTable.status] = status
            }
            if (updated == 0) return@suspendTransaction null

            ReservationTable.selectAll()
                .where { ReservationTable.id eq reservationId }
                .map { it.toReservation() }
                .singleOrNull()
        }

    override suspend fun deleteReservation(reservationId: Int): Boolean = suspendTransaction {
        ReservationTable.deleteWhere { ReservationTable.id eq reservationId } > 0
    }

    private fun ResultRow.toReservation(): Reservation = Reservation(
        id = this[ReservationTable.id],
        carId = this[ReservationTable.carId],
        renterId = this[ReservationTable.renterId],
        startTime = Instant.fromEpochMilliseconds(this[ReservationTable.startTime]),
        endTime = Instant.fromEpochMilliseconds(this[ReservationTable.endTime]),
        status = this[ReservationTable.status],
        createdAt = Instant.fromEpochMilliseconds(this[ReservationTable.createdAt])
    )
}
