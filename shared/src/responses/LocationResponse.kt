package shared.responses

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class LocationResponse(
    val carId: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant
)
