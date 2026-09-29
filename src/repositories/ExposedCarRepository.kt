package repositories

import mappers.normalizeLicensePlate
import mappers.toCar
import models.BEVCar
import models.Car
import models.FCEVCar
import models.ICECar
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import requests.CarChangeRequest
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

    override suspend fun getCarById(carId: Int): Car? = suspendTransaction {
        CarTable
            .leftJoin(IceCarTable, { CarTable.id }, { IceCarTable.carId })
            .leftJoin(BevCarTable, { CarTable.id }, { BevCarTable.carId })
            .leftJoin(FcevCarTable, { CarTable.id }, { FcevCarTable.carId })
            .selectAll()
            .where { CarTable.id eq carId }
            .singleOrNull()
            ?.toCar()
    }

    private fun getCarByIdQuery(carId: Int): Car? { //Important! without suspendTransaction
        return CarTable
            .leftJoin(IceCarTable, { CarTable.id }, { IceCarTable.carId })
            .leftJoin(BevCarTable, { CarTable.id }, { BevCarTable.carId })
            .leftJoin(FcevCarTable, { CarTable.id }, { FcevCarTable.carId })
            .selectAll()
            .where { CarTable.id eq carId }
            .singleOrNull()
            ?.toCar()
    }

    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = suspendTransaction {
        CarTable
            .leftJoin(IceCarTable, { CarTable.id }, { IceCarTable.carId })
            .leftJoin(BevCarTable, { CarTable.id }, { BevCarTable.carId })
            .leftJoin(FcevCarTable, { CarTable.id }, { FcevCarTable.carId })
            .selectAll()
            .where { CarTable.licensePlate eq normalizeLicensePlate(licensePlate) }
            .singleOrNull()
            ?.toCar()
    }

    override suspend fun createCar(car: Car): Car = suspendTransaction {
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
                    BevCarTable.insert {
                        it[BevCarTable.carId] = carId
                        it[BevCarTable.batteryCapacityKwh] = car.batteryCapacityKWh.toBigDecimal()
                    }
                }

                is FCEVCar -> {
                    FcevCarTable.insert {
                        it[FcevCarTable.carId] = carId
                        it[FcevCarTable.tankCapacityKgH2] = car.tankCapacityKgH2.toBigDecimal()
                    }
                }
            }

        getCarById(carId)!!// can this be solved a different way
        }

    override suspend fun updateCar(carId: Int, change: CarChangeRequest): Car? = suspendTransaction {
        val existingCar = getCarByIdQuery(carId)

        if (existingCar == null) {
            null
        } else {
            val carChanged = listOf(
                change.ownerId, change.licensePlate, change.brand, change.model,
                change.productionYear, change.trim, change.color, change.seats,
                change.doors, change.vehicleType, change.readyToDriveWeightKg,
                change.consumptionCombined, change.co2EmissionCombined
            ).any { it != null }

            if (carChanged) {
                CarTable.update({ CarTable.id eq carId }) {
                    change.ownerId?.let { value -> it[ownerId] = value }
                    change.licensePlate?.let { value -> it[licensePlate] = normalizeLicensePlate(value) }
                    change.brand?.let { value -> it[brand] = value }
                    change.model?.let { value -> it[model] = value }
                    change.productionYear?.let { value -> it[productionYear] = value.toShort() }
                    change.trim?.let { value -> it[trim] = value }
                    change.color?.let { value -> it[color] = value }
                    change.seats?.let { value -> it[seats] = value.toUByte() }
                    change.doors?.let { value -> it[doors] = value.toUByte() }
                    change.vehicleType?.let { value -> it[vehicleType] = value }
                    change.readyToDriveWeightKg?.let { value -> it[readyToDriveWeightKg] = value }
                    change.consumptionCombined?.let { value -> it[consumptionCombined] = value.toBigDecimal() }
                    change.co2EmissionCombined?.let { value -> it[co2EmissionCombined] = value.toBigDecimal() }
                }
            }

            when (existingCar) {
                is ICECar -> {
                    val iceChanged = change.fuelType != null ||
                            change.tankCapacityL != null ||
                            change.automaticTransmission != null

                    if (iceChanged) {
                        IceCarTable.update({ IceCarTable.carId eq carId }) {
                            change.fuelType?.let { value -> it[fuelType] = value }
                            change.tankCapacityL?.let { value -> it[tankCapacityL] = value.toBigDecimal() }
                            change.automaticTransmission?.let { value -> it[automaticTransmission] = value }
                        }
                    }
                }

                is BEVCar -> {
                    if (change.batteryCapacityKWh != null) {
                        BevCarTable.update({ BevCarTable.carId eq carId }) {
                            change.batteryCapacityKWh.let { value -> it[batteryCapacityKwh] = value.toBigDecimal() }
                        }
                    }
                }

                is FCEVCar -> {
                    if (change.tankCapacityKgH2 != null) {
                        FcevCarTable.update({ FcevCarTable.carId eq carId }) {
                            change.tankCapacityKgH2.let { value -> it[tankCapacityKgH2] = value.toBigDecimal() }
                        }
                    }
                }
            }

            CarTable
                .leftJoin(IceCarTable, { CarTable.id }, { IceCarTable.carId })
                .leftJoin(BevCarTable, { CarTable.id }, { BevCarTable.carId })
                .leftJoin(FcevCarTable, { CarTable.id }, { FcevCarTable.carId })
                .selectAll()
                .where { CarTable.id eq carId }
                .singleOrNull()
                ?.toCar()
        }
    }

    override suspend fun deleteCar(carId: Int): Boolean = suspendTransaction {
        // Important! Delete subtype and references first, they reference car.
        IceCarTable.deleteWhere { IceCarTable.carId eq carId }
        BevCarTable.deleteWhere { BevCarTable.carId eq carId }
        FcevCarTable.deleteWhere { FcevCarTable.carId eq carId }

        CarTable.deleteWhere { CarTable.id eq carId } > 0
    }

}