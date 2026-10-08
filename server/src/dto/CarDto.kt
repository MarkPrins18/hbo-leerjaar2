package dto

import kotlinx.serialization.Serializable
import models.FuelType
import models.VehicleType

@Serializable
data class CarDto(
    val licensePlate: String,
    val ownerId: Int,
    val trim: String? = null,
    val tankCapacityL: Double? = null,
    val automaticTransmission: Boolean? = null,
    val batteryCapacityKWh: Double? = null,
    val tankCapacityKgH2: Double? = null
)

@Serializable
data class CarChangeDto(
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

data class CarFilter(
    val maxPrice: Double? = null,
    val maxDistanceKm: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)