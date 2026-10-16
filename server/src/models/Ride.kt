package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Ride(
    val id: Int,
    val reservationId: Int,
    val startTime: Instant,
    val endTime: Instant?,
    val status: RideStatus,
    val distanceM: Double?,
    val avgAcceleration: Double?,
    val avgDeceleration: Double?,
    val bonusPoints: Int?,
)