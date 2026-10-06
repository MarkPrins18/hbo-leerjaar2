package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

@Serializable
data class Reservation(
    val id: Int,
    val carId: Int,
    val userId: Int,
    val startTime: Instant,
    val endTime: Instant,
    val status: ReservationStatus
)