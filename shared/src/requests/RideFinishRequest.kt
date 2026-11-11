package shared.requests

import kotlinx.serialization.Serializable

/** [latitude] and [longitude] are where the car is parked at the end of the ride. */
@Serializable
data class RideFinishRequest(
    val distanceM: Double,
    val avgAcceleration: Double,
    val avgDeceleration: Double,
    val latitude: Double,
    val longitude: Double
)
