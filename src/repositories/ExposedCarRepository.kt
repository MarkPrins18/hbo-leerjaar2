package repositories

import models.BEVCar
import models.Car
import models.FCEVCar
import models.ICECar
import org.jetbrains.exposed.v1.core.Query
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.BevCarTable
import tables.CarTable
import tables.FcevCarTable
import tables.IceCarTable
import java.math.BigDecimal

class ExposedCarRepository : CarRepository {

    // fix: gedeelde join-query, hergebruikt door getAllCars/getCarById/create/updateCar
    // zodat de leftJoin-opzet maar op 1 plek staat.
    private fun carJoinQuery(): Query =
        CarTable
            .leftJoin(IceCarTable, { CarTable.id }, { IceCarTable.carId })
            .leftJoin(BevCarTable, { CarTable.id }, { BevCarTable.carId })
            .leftJoin(FcevCarTable, { CarTable.id }, { FcevCarTable.carId })
            .selectAll()

    override suspend fun getAllCars(): List<Car> = suspendTransaction {
        carJoinQuery().map { row -> row.toCar() }
    }

    override suspend fun getCarById(carId: Int): Car? = suspendTransaction {
        carJoinQuery()
            .where { CarTable.id eq carId }
            .map { row -> row.toCar() }
            .singleOrNull()
    }

    override suspend fun createCar(car: Car): Car = suspendTransaction {
        val newId = CarTable.insert {
            it[ownerId] = car.ownerId
            it[licensePlate] = car.licensePlate
            it[brand] = car.brand
            it[model] = car.model
            it[productionYear] = car.productionYear.toShort()
            it[trim] = car.trim
            it[color] = car.color
            it[seats] = car.seats.toUByte()
        }[CarTable.id]

        insertSubtype(newId, car)

        carJoinQuery()
            .where { CarTable.id eq newId }
            .map { row -> row.toCar() }
            .single()
    }

    override suspend fun updateCar(car: Car): Car? = suspendTransaction {
        val carId = car.id ?: return@suspendTransaction null

        val updatedRows = CarTable.update({ CarTable.id eq carId }) {
            it[ownerId] = car.ownerId
            it[licensePlate] = car.licensePlate
            it[brand] = car.brand
            it[model] = car.model
            it[productionYear] = car.productionYear.toShort()
            it[trim] = car.trim
            it[color] = car.color
            it[seats] = car.seats.toUByte()
        }
        if (updatedRows == 0) return@suspendTransaction null

        // Bestaande subtype-rij(en) weggooien en opnieuw opbouwen: zo werkt het
        // ook correct als het brandstoftype van de auto verandert.
        IceCarTable.deleteWhere { IceCarTable.carId eq carId }
        BevCarTable.deleteWhere { BevCarTable.carId eq carId }
        FcevCarTable.deleteWhere { FcevCarTable.carId eq carId }
        insertSubtype(carId, car)

        carJoinQuery()
            .where { CarTable.id eq carId }
            .map { row -> row.toCar() }
            .singleOrNull()
    }

    override suspend fun deleteCar(carId: Int): Boolean = suspendTransaction {
        IceCarTable.deleteWhere { IceCarTable.carId eq carId }
        BevCarTable.deleteWhere { BevCarTable.carId eq carId }
        FcevCarTable.deleteWhere { FcevCarTable.carId eq carId }
        CarTable.deleteWhere { CarTable.id eq carId } > 0
    }

    private fun insertSubtype(carId: Int, car: Car) {
        when (car) {
            is ICECar -> IceCarTable.insert {
                it[IceCarTable.carId] = carId
                it[fuelType] = car.fuelType
                it[tankCapacityL] = BigDecimal.valueOf(car.tankCapacityL)
                it[automaticTransmission] = car.automaticTransmission
            }

            is BEVCar -> BevCarTable.insert {
                it[BevCarTable.carId] = carId
                it[batteryCapacityKwh] = BigDecimal.valueOf(car.batteryCapacityKWh)
            }

            is FCEVCar -> FcevCarTable.insert {
                it[FcevCarTable.carId] = carId
                it[tankCapacityKgH2] = BigDecimal.valueOf(car.tankCapacityKgH2)
            }
        }
    }

    private fun ResultRow.toCar(): Car = when {
        getOrNull(IceCarTable.carId) != null -> ICECar(
            id = this[CarTable.id],
            ownerId = this[CarTable.ownerId],
            licensePlate = this[CarTable.licensePlate],
            brand = this[CarTable.brand],
            model = this[CarTable.model],
            // fix: was `year = ...` -- de constructorparameter heet productionYear,
            // dit compileerde niet (er bestaat geen parameter met de naam "year").
            productionYear = this[CarTable.productionYear].toInt(),
            trim = this[CarTable.trim],
            color = this[CarTable.color],
            seats = this[CarTable.seats].toInt(),
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
            seats = this[CarTable.seats].toInt(),
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
            seats = this[CarTable.seats].toInt(),
            tankCapacityKgH2 = this[FcevCarTable.tankCapacityKgH2].toDouble()
        )

        else -> error("Car ${this[CarTable.id]} heeft geen bijbehorend subtype in ice_car, bev_car of fcev_car")
    }

}
