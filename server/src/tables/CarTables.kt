package tables

import models.FuelType
import models.VehicleType
import org.jetbrains.exposed.v1.core.Table

val carTables = listOf(CarTable, IceCarTable, BevCarTable, FcevCarTable)

object CarTable : Table("car") {
    val id = integer("id").autoIncrement()
    val ownerId = integer("owner_id") references OwnerTable.id
    val licensePlate = varchar("license_plate", 20).uniqueIndex()
    val brand = varchar("brand", 100)
    val model = varchar("model", 100)
    val productionYear = short("production_year")
    val trim = varchar("trim", 100).nullable()
    val color = varchar("color", 50)
    val seats = ubyte("seats").nullable()
    val doors = ubyte("doors").nullable()
    val vehicleType = enumerationByName("vehicle_type", 30, VehicleType::class)
    val readyToDriveWeightKg = integer("ready_to_drive_weight_kg")
    val consumptionCombined = decimal("consumption_combined", 6, 2).nullable()
    val co2EmissionCombined = decimal("co2_emission_combined", 7, 2).nullable()

    override val primaryKey = PrimaryKey(id)
}

object IceCarTable : Table("ice_car") {
    val carId = integer("car_id") references CarTable.id
    val fuelType = enumerationByName("fuel_type", 10, FuelType::class)
    val tankCapacityL = decimal("tank_capacity_l", 6, 2)
    val automaticTransmission = bool("automatic_transmission")

    override val primaryKey = PrimaryKey(carId)
}

object BevCarTable : Table("bev_car") {
    val carId = integer("car_id") references CarTable.id
    val batteryCapacityKwh = decimal("battery_capacity_kwh", 7, 2)

    override val primaryKey = PrimaryKey(carId)
}

object FcevCarTable : Table("fcev_car") {
    val carId = integer("car_id") references CarTable.id
    val tankCapacityKgH2 = decimal("tank_capacity_kg_h2", 6, 2)

    override val primaryKey = PrimaryKey(carId)
}