package dto

import kotlinx.serialization.Serializable

@Serializable
data class RideFinishRequest(
    val distanceM: Double,
    val avgAcceleration: Double,
    val avgDeceleration: Double
)
