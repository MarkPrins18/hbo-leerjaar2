package mappers

import models.*
import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto
import services.UnprocessableException


/**
 * Normalizes a license plate to the RDW notation: no dashes, uppercase.
 * For example "r-816-lf" becomes "R816LF".
 */
fun normalizeLicensePlate(licensePlate: String): String =
    licensePlate.replace("-", "").uppercase()

fun RdwVehicleDto.toCar(
    brandstoffen: List<RdwFuelDto>,
    id: Int,
    ownerId: Int,
    trim: String?,
    tankCapacityL: Double? = null,
    automaticTransmission: Boolean? = null,
    batteryCapacityKWh: Double? = null,
    tankCapacityKgH2: Double? = null
): Car {
    val hoofdbrandstof = if (brandstoffen.size > 1) {
        // Hybrid: multiple fuel rows (incl. Elektriciteit). We treat hybrids
        // as ICE, so we pick the non-electric row.
        brandstoffen.firstOrNull { it.fuelDescription != "Elektriciteit" }
            ?: throw UnprocessableException("Geen niet-elektrische brandstof gevonden voor hybride kenteken $licensePlate")
    } else {
        // Not a hybrid: just take the main fuel at sequence number 1
        brandstoffen.firstOrNull { it.fuelSequenceNumber == "1" }
            ?: throw UnprocessableException("Geen hoofdbrandstof gevonden voor kenteken $licensePlate")
    }

    val vehicleType = mapVehicleType(this.vehicleType)

    val readyToDriveWeightKg = this.readyToDriveWeightKg?.toIntOrNull()
        ?: throw UnprocessableException("Geen massa rijklaar bekend voor kenteken $licensePlate")

    val consumptionCombined = hoofdbrandstof.consumptionCombined?.toDoubleOrNull() //No throw!!
    val co2EmissionCombined = hoofdbrandstof.co2EmissionCombined?.toDoubleOrNull()

    val productionYear = parseProductionYear(firstAdmissionDate)
    val seats = this.seats?.toIntOrNull()
    val doors = this.doors?.toIntOrNull()

    return when (val fuelDescription = hoofdbrandstof.fuelDescription) {
        "Elektriciteit" -> BEVCar(
            id = id, ownerId = ownerId,
            licensePlate = licensePlate,
            brand = brand, model = model,
            productionYear = productionYear, trim = trim, color = color,
            seats = seats, doors = doors, vehicleType = vehicleType,
            readyToDriveWeightKg = readyToDriveWeightKg,
            consumptionCombined = consumptionCombined,
            co2EmissionCombined = co2EmissionCombined,
            batteryCapacityKWh = batteryCapacityKWh
                ?: throw UnprocessableException("batteryCapacityKWh is verplicht voor een BEV")
        )
        "Waterstof" -> FCEVCar(
            id = id, ownerId = ownerId,
            licensePlate = licensePlate,
            brand = brand, model = model,
            productionYear = productionYear, trim = trim, color = color,
            seats = seats, doors = doors, vehicleType = vehicleType,
            readyToDriveWeightKg = readyToDriveWeightKg,
            consumptionCombined = consumptionCombined,
            co2EmissionCombined = co2EmissionCombined,
            tankCapacityKgH2 = tankCapacityKgH2
                ?: throw UnprocessableException("tankCapacityKgH2 is verplicht voor een FCEV")
        )
        "Benzine", "Diesel", "LPG" -> ICECar(
            id = id, ownerId = ownerId,
            licensePlate = licensePlate,
            brand = brand, model = model,
            productionYear = productionYear, trim = trim, color = color,
            seats = seats, doors = doors, vehicleType = vehicleType,
            readyToDriveWeightKg = readyToDriveWeightKg,
            consumptionCombined = consumptionCombined,
            co2EmissionCombined = co2EmissionCombined,
            fuelType = mapFuelType(fuelDescription),
            tankCapacityL = tankCapacityL
                ?: throw UnprocessableException("tankCapacityL is verplicht voor een ICE-auto"),
            automaticTransmission = automaticTransmission
                ?: throw UnprocessableException("automaticTransmission is verplicht voor een ICE-auto")
        )
        else -> throw UnprocessableException("Niet-ondersteunde brandstof: $fuelDescription")
    }
}

private fun mapVehicleType(voertuigsoort: String): VehicleType = when (voertuigsoort) {
    "Personenauto" -> VehicleType.PASSENGER_CAR
    "Bedrijfsauto" -> VehicleType.LIGHT_COMMERCIAL_VEHICLE
    else -> throw UnprocessableException("Onbekend voertuigsoort: $voertuigsoort")
}

private fun mapFuelType(fuelDescription: String): FuelType = when (fuelDescription) {
    "Benzine" -> FuelType.PETROL
    "Diesel" -> FuelType.DIESEL
    "LPG" -> FuelType.LPG
    else -> throw UnprocessableException("Niet-ondersteunde brandstof: $fuelDescription")
}

private fun parseProductionYear(firstAdmissionDate: String): Int {
    if (firstAdmissionDate.length < 4) {
        throw UnprocessableException("Onbekend datumformaat voor datum_eerste_toelating: $firstAdmissionDate")
    }
    return firstAdmissionDate.take(4).toIntOrNull()
        ?: throw UnprocessableException("Kon jaartal niet parsen uit datum_eerste_toelating: $firstAdmissionDate")
}
