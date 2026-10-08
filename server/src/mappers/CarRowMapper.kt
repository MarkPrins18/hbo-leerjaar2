package mappers

import models.*
import org.jetbrains.exposed.v1.core.ResultRow
import tables.*

fun ResultRow.toCar(): Car = when {
    getOrNull(IceCarTable.carId) != null -> ICECar(
        id = this[CarTable.id],
        ownerId = this[CarTable.ownerId],
        licensePlate = this[CarTable.licensePlate],
        brand = this[CarTable.brand],
        model = this[CarTable.model],
        productionYear = this[CarTable.productionYear].toInt(),
        trim = this[CarTable.trim],
        color = this[CarTable.color],
        seats = this[CarTable.seats]?.toInt(),
        doors = this[CarTable.doors]?.toInt(),
        vehicleType = this[CarTable.vehicleType],
        readyToDriveWeightKg = this[CarTable.readyToDriveWeightKg],
        consumptionCombined = this[CarTable.consumptionCombined]?.toDouble(),
        co2EmissionCombined = this[CarTable.co2EmissionCombined]?.toDouble(),
        fuelType = this[IceCarTable.fuelType],
        tankCapacityL = this[IceCarTable.tankCapacityL].toDouble(),
        automaticTransmission = this[IceCarTable.automaticTransmission]
    )

    getOrNull(BevCarTable.carId) != null -> BEVCar(
        id = this[CarTable.id],
        ownerId = this[CarTable.ownerId],
        licensePlate = this[CarTable.licensePlate],
        brand = this[CarTable.brand],
        model = this[CarTable.model],
        productionYear = this[CarTable.productionYear].toInt(),
        trim = this[CarTable.trim],
        color = this[CarTable.color],
        seats = this[CarTable.seats]?.toInt(),
        doors = this[CarTable.doors]?.toInt(),
        vehicleType = this[CarTable.vehicleType],
        readyToDriveWeightKg = this[CarTable.readyToDriveWeightKg],
        consumptionCombined = this[CarTable.consumptionCombined]?.toDouble(),
        co2EmissionCombined = this[CarTable.co2EmissionCombined]?.toDouble(),
        batteryCapacityKWh = this[BevCarTable.batteryCapacityKwh].toDouble()
    )

    getOrNull(FcevCarTable.carId) != null -> FCEVCar(
        id = this[CarTable.id],
        ownerId = this[CarTable.ownerId],
        licensePlate = this[CarTable.licensePlate],
        brand = this[CarTable.brand],
        model = this[CarTable.model],
        productionYear = this[CarTable.productionYear].toInt(),
        trim = this[CarTable.trim],
        color = this[CarTable.color],
        seats = this[CarTable.seats]?.toInt(),
        doors = this[CarTable.doors]?.toInt(),
        vehicleType = this[CarTable.vehicleType],
        readyToDriveWeightKg = this[CarTable.readyToDriveWeightKg],
        consumptionCombined = this[CarTable.consumptionCombined]?.toDouble(),
        co2EmissionCombined = this[CarTable.co2EmissionCombined]?.toDouble(),
        tankCapacityKgH2 = this[FcevCarTable.tankCapacityKgH2].toDouble()
    )

    else -> error("Car ${this[CarTable.id]} heeft geen bijbehorend subtype in ice_car, bev_car of fcev_car")
}