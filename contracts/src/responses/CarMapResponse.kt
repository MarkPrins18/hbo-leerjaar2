package contracts.responses

import kotlinx.serialization.Serializable
import contracts.models.Car

@Serializable
data class CarMapResponse(
    val car: Car,
    val latitude: Double?,
    val longitude: Double?,
    val pricePerDay: Double?
)
