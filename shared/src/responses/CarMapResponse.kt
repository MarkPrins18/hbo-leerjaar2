package shared.responses

import kotlinx.serialization.Serializable
import shared.models.Car

@Serializable
data class CarMapResponse(
    val car: Car,
    val latitude: Double?,
    val longitude: Double?,
    val pricePerDay: Double?
)
