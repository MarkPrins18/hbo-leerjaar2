package repositories

import shared.models.Reservation
import shared.models.ReservationStatus
import kotlin.time.Instant

interface ReservationRepository {
    /** Locks the car, checks for an overlapping active reservation and inserts in one transaction. Returns null on overlap. */
    suspend fun createReservationIfAvailable(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus, totalPrice: Double
    ): Reservation?

    suspend fun getReservationById(id: Int): Reservation?
    suspend fun getReservationsByCarAndUser(carId: Int, userId: Int): List<Reservation>
    suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation?
    suspend fun getReservationsByUserId(userId: Int): List<Reservation>
}
