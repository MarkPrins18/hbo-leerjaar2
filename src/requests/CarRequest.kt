package requests

import kotlinx.serialization.Serializable

@Serializable
data class CarRequest(
    val licensePlate: String,
    val ownerId: Int,
    val trim: String? = null,
    val tankCapacityL: Double? = null,
    val automaticTransmission: Boolean? = null,
    val batteryCapacityKWh: Double? = null,
    val tankCapacityKgH2: Double? = null
)