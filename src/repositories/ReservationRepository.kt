package repositories

import models.Reservation
import models.ReservationStatus
import kotlin.time.Instant

interface ReservationRepository {
    suspend fun carExists(carId: Int): Boolean
    suspend fun userExists(userId: Int): Boolean
    suspend fun hasActiveOverlap(carId: Int, startTime: Instant, endTime: Instant): Boolean
    suspend fun createReservation(carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus): Reservation
    suspend fun getReservationById(id: Int): Reservation?
    suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation?
}