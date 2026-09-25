package repositories

import models.BEVCar
import models.Car
import models.FCEVCar
import models.ICECar
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import tables.BevCarTable
import tables.CarTable
import tables.FcevCarTable
import tables.IceCarTable

class ExposedCarRepository : CarRepository {

    override suspend fun getAllCars(): List<Car> = suspendTransaction {
        CarTable
            .leftJoin(IceCarTable, { CarTable.id }, { IceCarTable.carId })
            .leftJoin(BevCarTable, { CarTable.id }, { BevCarTable.carId })
            .leftJoin(FcevCarTable, { CarTable.id }, { FcevCarTable.carId })
            .selectAll()
            .map { row -> row.toCar() }
    }

    override suspend fun getCarById(carId: Int): Car? {
        TODO("Volgt in een latere stap")
    }

    override suspend fun createCar(car: Car): Car {
        return suspendTransaction {
            val insertedCar = CarTable.insert {
                it[ownerId] = car.ownerId
                it[licensePlate] = car.licensePlate
                it[brand] = car.brand
                it[model] = car.model
                it[productionYear] = car.productionYear.toShort()
                it[trim] = car.trim
                it[color] = car.color
                it[seats] = car.seats?.toUByte()
                it[doors] = car.doors?.toUByte()
                it[vehicleType] = car.vehicleType
                it[readyToDriveWeightKg] = car.readyToDriveWeightKg
                it[consumptionCombined] = car.consumptionCombined?.toBigDecimal()
                it[co2EmissionCombined] = car.co2EmissionCombined?.toBigDecimal()
            }

            val carId = insertedCar[CarTable.id]

            when (car) {
                is ICECar -> {
                    IceCarTable.insert {
                        it[IceCarTable.carId] = carId
                        it[IceCarTable.fuelType] = car.fuelType
                        it[IceCarTable.tankCapacityL] = car.tankCapacityL.toBigDecimal()
                        it[IceCarTable.automaticTransmission] = car.automaticTransmission
                    }
                }

                is BEVCar -> {
                    // later
                }

                is FCEVCar -> {
                    // later
                }
            }

            car
        }
    }

    override suspend fun updateCar(car: Car): Car? {
        TODO("Volgt in een latere stap")
    }

    override suspend fun deleteCar(carId: Int): Boolean {
        TODO("Volgt in een latere stap")
    }

    private fun ResultRow.toCar(): Car = when {
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

}