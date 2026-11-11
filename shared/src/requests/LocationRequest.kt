package shared.requests

import kotlinx.serialization.Serializable

@Serializable
data class LocationRequest(
    val latitude: Double,
    val longitude: Double
)
