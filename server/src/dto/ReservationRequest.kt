package requests

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ReservationRequest(
    val carId: Int,
    val userId: Int,
    val startTime: Instant,
    val endTime: Instant
)