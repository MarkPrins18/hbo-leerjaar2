package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FuelType {
    PETROL, DIESEL, LPG
}

/**
 * The RDW-based vehicle type ("voertuigsoort"), used to determine
 * which MRB (Dutch road tax) rate structure applies.
 */
@Serializable
enum class VehicleType {
    PASSENGER_CAR,
    LIGHT_COMMERCIAL_VEHICLE
}

@Serializable
sealed class Car {
    abstract val id: Int
    abstract val ownerId: Int
    abstract val licensePlate: String
    abstract val brand: String
    abstract val model: String
    abstract val productionYear: Int
    abstract val trim: String?
    abstract val color: String
    abstract val seats: Int?
    abstract val doors: Int?
    abstract val vehicleType: VehicleType

    /**
     * Ready-to-drive weight in kg (RDW "massa rijklaar"): curb weight including
     * fuel, fluids and driver. Required input for the Dutch MRB (road tax) calculation.
     */
    abstract val readyToDriveWeightKg: Int

    /**
     * Combined consumption per 100 km. Unit differs per subtype:
     * - ICECar: liters per 100 km
     * - BEVCar: kWh per 100 km
     * - FCEVCar: kg H2 per 100 km
     */
    abstract val consumptionCombined: Double?

    /** Combined CO2 emission in g/km. Used for MRB discounts on EV/PHEV vehicles. */
    abstract val co2EmissionCombined: Double?
}

@Serializable
@SerialName("ICECar")
data class ICECar(
    override val id: Int,
    override val ownerId: Int,
    override val licensePlate: String,
    override val brand: String,
    override val model: String,
    override val productionYear: Int,
    override val trim: String?,
    override val color: String,
    override val seats: Int?,
    override val doors: Int?,
    override val vehicleType: VehicleType,
    override val readyToDriveWeightKg: Int,
    override val consumptionCombined: Double?,
    override val co2EmissionCombined: Double?,
    val fuelType: FuelType,
    val tankCapacityL: Double,
    val automaticTransmission: Boolean
) : Car()

@Serializable
@SerialName("BEVCar")
data class BEVCar(
    override val id: Int,
    override val ownerId: Int,
    override val licensePlate: String,
    override val brand: String,
    override val model: String,
    override val productionYear: Int,
    override val trim: String?,
    override val color: String,
    override val seats: Int?,
    override val doors: Int?,
    override val vehicleType: VehicleType,
    override val readyToDriveWeightKg: Int,
    override val consumptionCombined: Double?,
    override val co2EmissionCombined: Double?,
    val batteryCapacityKWh: Double
) : Car()

@Serializable
@SerialName("FCEVCar")
data class FCEVCar(
    override val id: Int,
    override val ownerId: Int,
    override val licensePlate: String,
    override val brand: String,
    override val model: String,
    override val productionYear: Int,
    override val trim: String?,
    override val color: String,
    override val seats: Int?,
    override val doors: Int?,
    override val vehicleType: VehicleType,
    override val readyToDriveWeightKg: Int,
    override val consumptionCombined: Double?,
    override val co2EmissionCombined: Double?,
    val tankCapacityKgH2: Double
) : Car()