package requests

import kotlinx.serialization.Serializable
import models.FuelType
import models.VehicleType

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

@Serializable
data class CarChangeRequest(
    val licensePlate: String? = null,
    val ownerId: Int? = null,
    val brand: String? = null,
    val model: String? = null,
    val productionYear: Int? = null,
    val trim: String? = null,
    val color: String? = null,
    val seats: Int? = null,
    val doors: Int? = null,
    val vehicleType: VehicleType? = null,
    val readyToDriveWeightKg: Int? = null,
    val consumptionCombined: Double? = null,
    val co2EmissionCombined: Double? = null,
    val fuelType: FuelType? = null,
    val tankCapacityL: Double? = null,
    val automaticTransmission: Boolean? = null,
    val batteryCapacityKWh: Double? = null,
    val tankCapacityKgH2: Double? = null
)