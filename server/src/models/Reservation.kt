package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Reservation(
    val id: Int,
    val carId: Int,
    val userId: Int,
    val startTime: Instant,
    val endTime: Instant,
    val status: ReservationStatus
)