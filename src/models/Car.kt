package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FuelType {
    PETROL, DIESEL, LPG
}

@Serializable
sealed class Car{
    abstract val id: Int?  //let op ?
    abstract val ownerId: Int
    abstract val licensePlate: String
    abstract val brand: String
    abstract val model: String
    abstract val year: Int
    abstract val trim: String?
    abstract val color: String
    abstract val seats: Int
}

@Serializable
@SerialName("ICECar")
data class ICECar(
    override val id: Int,
    override val ownerId: Int,
    override val licensePlate: String,
    override val brand: String,
    override val model: String,
    override val year: Int,
    override val trim: String?,
    override val color: String,
    override val seats: Int,
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
    override val year: Int,
    override val trim: String?,
    override val color: String,
    override val seats: Int,
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
    override val year: Int,
    override val trim: String?,
    override val color: String,
    override val seats: Int,
    val tankCapacityKgH2: Double
) : Car()