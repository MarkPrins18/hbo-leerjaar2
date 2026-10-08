package services

import io.ktor.server.plugins.BadRequestException
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mappers.normalizeLicensePlate
import models.*
import dto.*
import kotlin.time.Clock

// Upper limits follow the database columns: decimal(6,2) = 9999.99, decimal(7,2) = 99999.99
private const val LICENSE_PLATE_MAX_LENGTH = 20
private const val NAME_MAX_LENGTH = 100
private const val COLOR_MAX_LENGTH = 50
private val SEATS = 1..9
private val DOORS = 0..9
private val READY_TO_DRIVE_WEIGHT_KG = 1..10_000
private val CONSUMPTION_COMBINED = 0.0..9999.99
private val CO2_EMISSION_COMBINED = 0.0..99999.99
private val TANK_CAPACITY_L = 0.01..9999.99
private val BATTERY_CAPACITY_KWH = 0.01..99999.99
private val TANK_CAPACITY_KG_H2 = 0.01..9999.99

fun CarDto.validate() {
    checkText("licensePlate", normalizeLicensePlate(licensePlate), LICENSE_PLATE_MAX_LENGTH)
    checkRange("ownerId", ownerId, 1..Int.MAX_VALUE)
    trim?.let { checkText("trim", it, NAME_MAX_LENGTH) }
    tankCapacityL?.let { checkRange("tankCapacityL", it, TANK_CAPACITY_L) }
    batteryCapacityKWh?.let { checkRange("batteryCapacityKWh", it, BATTERY_CAPACITY_KWH) }
    tankCapacityKgH2?.let { checkRange("tankCapacityKgH2", it, TANK_CAPACITY_KG_H2) }
}

fun CarChangeDto.validate(car: Car) {
    checkFieldsMatchSubtype(car)

    licensePlate?.let { checkText("licensePlate", normalizeLicensePlate(it), LICENSE_PLATE_MAX_LENGTH) }
    ownerId?.let { checkRange("ownerId", it, 1..Int.MAX_VALUE) }
    brand?.let { checkText("brand", it, NAME_MAX_LENGTH) }
    model?.let { checkText("model", it, NAME_MAX_LENGTH) }
    trim?.let { checkText("trim", it, NAME_MAX_LENGTH) }
    color?.let { checkText("color", it, COLOR_MAX_LENGTH) }

    productionYear?.let { checkRange("productionYear", it, 1900..currentYear() + 1) }
    seats?.let { checkRange("seats", it, SEATS) }
    doors?.let { checkRange("doors", it, DOORS) }
    readyToDriveWeightKg?.let { checkRange("readyToDriveWeightKg", it, READY_TO_DRIVE_WEIGHT_KG) }

    consumptionCombined?.let { checkRange("consumptionCombined", it, CONSUMPTION_COMBINED) }
    co2EmissionCombined?.let { checkRange("co2EmissionCombined", it, CO2_EMISSION_COMBINED) }
    tankCapacityL?.let { checkRange("tankCapacityL", it, TANK_CAPACITY_L) }
    batteryCapacityKWh?.let { checkRange("batteryCapacityKWh", it, BATTERY_CAPACITY_KWH) }
    tankCapacityKgH2?.let { checkRange("tankCapacityKgH2", it, TANK_CAPACITY_KG_H2) }
}

private fun CarChangeDto.checkFieldsMatchSubtype(car: Car) {
    val iceFieldsChanged = fuelType != null || tankCapacityL != null || automaticTransmission != null
    val bevFieldsChanged = batteryCapacityKWh != null
    val fcevFieldsChanged = tankCapacityKgH2 != null

    val invalid = when (car) {
        is ICECar -> bevFieldsChanged || fcevFieldsChanged
        is BEVCar -> iceFieldsChanged || fcevFieldsChanged
        is FCEVCar -> iceFieldsChanged || bevFieldsChanged
    }
    if (invalid) {
        val type = when (car) {
            is ICECar -> "ICE"
            is BEVCar -> "BEV"
            is FCEVCar -> "FCEV"
        }
        throw BadRequestException("Deze velden kunnen niet worden aangepast voor een $type-auto")
    }
}

fun CarFilter.validate() {
    maxPrice?.let { checkNotNegative("maxPrice", it) }
    maxDistanceKm?.let { checkNotNegative("maxDistanceKm", it) }
    latitude?.let { checkRange("latitude", it, -90.0..90.0) }
    longitude?.let { checkRange("longitude", it, -180.0..180.0) }

    val distanceParameters = listOf(maxDistanceKm, latitude, longitude)
    if (distanceParameters.any { it != null } && distanceParameters.any { it == null }) {
        throw BadRequestException("'maxDistanceKm', 'latitude' en 'longitude' moeten samen worden opgegeven")
    }
}

private fun checkNotNegative(name: String, value: Double) {
    if (!value.isFinite() || value < 0) {
        throw BadRequestException("'$name' moet een getal van 0 of hoger zijn")
    }
}

private fun checkText(name: String, value: String, maxLength: Int) {
    if (value.isBlank()) {
        throw BadRequestException("'$name' mag niet leeg zijn")
    }
    if (value.length > maxLength) {
        throw BadRequestException("'$name' mag maximaal $maxLength tekens zijn")
    }
}

private fun checkRange(name: String, value: Int, range: IntRange) {
    if (value !in range) {
        throw BadRequestException("'$name' moet tussen ${range.first} en ${range.last} liggen")
    }
}

private fun checkRange(name: String, value: Double, range: ClosedFloatingPointRange<Double>) {
    if (value !in range) {
        throw BadRequestException("'$name' moet tussen ${range.start} en ${range.endInclusive} liggen")
    }
}

private fun currentYear(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
