package services

import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mappers.normalizeLicensePlate
import models.*
import dto.*
import models.gemini.CarCostEstimate
import kotlin.time.Clock

// Upper limits follow the database columns: decimal(6,2) = 9999.99, decimal(7,2) = 99999.99
private const val LICENSE_PLATE_MAX_LENGTH = 20
private val DUTCH_LICENSE_PLATE = Regex("[A-Z0-9]{6}")
private const val NAME_MAX_LENGTH = 100
private const val COLOR_MAX_LENGTH = 50
private val SEATS = 1..9
private val DOORS = 0..9
private val READY_TO_DRIVE_WEIGHT_KG = 1..10_000
private val CONSUMPTION_COMBINED = 0.0..9999.99
private val CO2_EMISSION_COMBINED = 0.0..99999.99
private val ROAD_TAX_PER_QUARTER = 0.0..99999.99
private val TANK_CAPACITY_L = 0.01..9999.99
private val BATTERY_CAPACITY_KWH = 0.01..99999.99
private val TANK_CAPACITY_KG_H2 = 0.01..9999.99
internal const val MAX_PHOTO_BYTES = 5 * 1024 * 1024
private const val LOCATION_MAX_LENGTH = 255
private val PRICE_PER_DAY = 0.01..999999.99
private val DISTANCE_KM = 0.0..1_000_000.0

/** The RDW only knows Dutch license plates, so check the format before calling it. */
fun CarDto.validate() {
    val normalizedLicensePlate = normalizeLicensePlate(licensePlate)
    checkText("licensePlate", normalizedLicensePlate, LICENSE_PLATE_MAX_LENGTH)
    if (!normalizedLicensePlate.matches(DUTCH_LICENSE_PLATE)) {
        throw ValidationException("'licensePlate' moet een Nederlands kenteken zijn van 6 letters en cijfers, bijvoorbeeld AB-123-C")
    }
    trim?.let { checkText("trim", it, NAME_MAX_LENGTH) }
    tankCapacityL?.let { checkRange("tankCapacityL", it, TANK_CAPACITY_L) }
    batteryCapacityKWh?.let { checkRange("batteryCapacityKWh", it, BATTERY_CAPACITY_KWH) }
    tankCapacityKgH2?.let { checkRange("tankCapacityKgH2", it, TANK_CAPACITY_KG_H2) }
    consumptionCombined?.let { checkRange("consumptionCombined", it, CONSUMPTION_COMBINED) }
    roadTaxPerQuarter?.let { checkRange("roadTaxPerQuarter", it, ROAD_TAX_PER_QUARTER) }
}

fun CarChangeDto.validate(car: Car) {
    checkFieldsMatchSubtype(car)

    licensePlate?.let { checkText("licensePlate", normalizeLicensePlate(it), LICENSE_PLATE_MAX_LENGTH) }
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
    roadTaxPerQuarter?.let { checkRange("roadTaxPerQuarter", it, ROAD_TAX_PER_QUARTER) }
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
        throw ValidationException("Deze velden kunnen niet worden aangepast voor een $type-auto")
    }
}

/** RDW values that don't fit the database columns are the RDW data's fault, not the client's. */
fun Car.validateRdwData() {
    val valid = brand.length <= NAME_MAX_LENGTH && model.length <= NAME_MAX_LENGTH && color.length <= COLOR_MAX_LENGTH
    if (!valid) {
        throw UnprocessableException("De RDW-gegevens van kenteken $licensePlate passen niet in de database")
    }
}

/** Basic sanity check on the Gemini answer: an out-of-range value is the AI's fault, not the client's. */
fun CarCostEstimate.validate() {
    val valid = roadTaxPerQuarter.isNullOrIn(ROAD_TAX_PER_QUARTER) && consumptionCombined.isNullOrIn(CONSUMPTION_COMBINED)
    if (!valid) {
        throw ExternalServiceException("Gemini gaf een ongeldig antwoord")
    }
}

private fun Double?.isNullOrIn(range: ClosedFloatingPointRange<Double>) = this == null || this in range

/** NaN and infinity fall outside the range as well. */
fun validateDistanceKm(distanceKm: Double) {
    checkRange("distanceKm", distanceKm, DISTANCE_KM)
}

fun validateAnnualKm(annualKm: Double) {
    checkRange("annualKm", annualKm, DISTANCE_KM)
}

fun CarFilter.validate() {
    maxPrice?.let { checkNotNegative("maxPrice", it) }
    maxDistanceKm?.let { checkNotNegative("maxDistanceKm", it) }
    latitude?.let { checkRange("latitude", it, -90.0..90.0) }
    longitude?.let { checkRange("longitude", it, -180.0..180.0) }

    val distanceParameters = listOf(maxDistanceKm, latitude, longitude)
    if (distanceParameters.any { it != null } && distanceParameters.any { it == null }) {
        throw ValidationException("'maxDistanceKm', 'latitude' en 'longitude' moeten samen worden opgegeven")
    }
}

/** Accepts JPEG only (Android camera default), recognised by its FF D8 FF signature. */
fun ByteArray.validatePhoto() {
    if (isEmpty()) {
        throw ValidationException("Foto is leeg")
    }
    if (size > MAX_PHOTO_BYTES) {
        throw FileTooLargeException("Foto mag maximaal ${MAX_PHOTO_BYTES / (1024 * 1024)} MB zijn")
    }
    val isJpeg = size >= 3 && this[0] == 0xFF.toByte() && this[1] == 0xD8.toByte() && this[2] == 0xFF.toByte()
    if (!isJpeg) {
        throw UnsupportedFileTypeException("Alleen JPEG-foto's zijn toegestaan")
    }
}

fun RentalTermsRequest.validate() {
    checkRange("pricePerDay", pricePerDay, PRICE_PER_DAY)
    checkText("pickupLocation", pickupLocation, LOCATION_MAX_LENGTH)
    returnLocation?.let { checkText("returnLocation", it, LOCATION_MAX_LENGTH) }
    checkTimeWindow("pickupTime", pickupTimeStart, pickupTimeEnd)
    checkTimeWindow("returnTime", returnTimeStart, returnTimeEnd)
}

private fun checkTimeWindow(name: String, start: LocalTime?, end: LocalTime?) {
    if ((start == null) != (end == null)) {
        throw ValidationException("'${name}Start' en '${name}End' moeten samen worden opgegeven")
    }
    if (start != null && end != null && start >= end) {
        throw ValidationException("'${name}End' moet na '${name}Start' liggen")
    }
}

private fun checkNotNegative(name: String, value: Double) {
    if (!value.isFinite() || value < 0) {
        throw ValidationException("'$name' moet een getal van 0 of hoger zijn")
    }
}

private fun checkText(name: String, value: String, maxLength: Int) {
    if (value.isBlank()) {
        throw ValidationException("'$name' mag niet leeg zijn")
    }
    if (value.length > maxLength) {
        throw ValidationException("'$name' mag maximaal $maxLength tekens zijn")
    }
}

private fun checkRange(name: String, value: Int, range: IntRange) {
    if (value !in range) {
        throw ValidationException("'$name' moet tussen ${range.first} en ${range.last} liggen")
    }
}

private fun checkRange(name: String, value: Double, range: ClosedFloatingPointRange<Double>) {
    if (value !in range) {
        throw ValidationException("'$name' moet tussen ${range.start} en ${range.endInclusive} liggen")
    }
}

private fun currentYear(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
