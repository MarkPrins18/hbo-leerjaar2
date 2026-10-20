package mappers

import models.*
import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto
import services.UnprocessableException

fun normalizeLicensePlate(licensePlate: String): String =
    licensePlate.filterNot { it == '-' || it.isWhitespace() }.uppercase()

fun RdwVehicleDto.toCar(
    brandstoffen: List<RdwFuelDto>,
    id: Int,
    userId: Int,
    trim: String?,
    tankCapacityL: Double? = null,
    automaticTransmission: Boolean? = null,
    batteryCapacityKWh: Double? = null,
    tankCapacityKgH2: Double? = null,
    consumptionCombined: Double? = null,
    roadTaxPerQuarter: Double? = null
): Car {
    val hoofdbrandstof = if (brandstoffen.size > 1) {
        // Hybrid: multiple fuel rows (incl. Elektriciteit). We treat hybrids
        // as ICE, so we pick the non-electric row.
        brandstoffen.firstOrNull { it.fuelDescription != "Elektriciteit" }
            ?: throw UnprocessableException("Geen niet-elektrische brandstof gevonden voor hybride kenteken $licensePlate")
    } else {
        brandstoffen.firstOrNull { it.fuelSequenceNumber == "1" }
            ?: throw UnprocessableException("Geen hoofdbrandstof gevonden voor kenteken $licensePlate")
    }

    val vehicleType = mapVehicleType(this.vehicleType)

    val readyToDriveWeightKg = this.readyToDriveWeightKg?.toIntOrNull()
        ?: throw UnprocessableException("Geen massa rijklaar bekend voor kenteken $licensePlate")

    val combinedConsumption = consumptionCombined ?: hoofdbrandstof.consumptionCombined?.toDoubleOrNull()
    val co2EmissionCombined = hoofdbrandstof.co2EmissionCombined?.toDoubleOrNull()

    val productionYear = parseProductionYear(firstAdmissionDate)
    val seats = this.seats?.toIntOrNull()
    val doors = this.doors?.toIntOrNull()

    return when (val fuelDescription = hoofdbrandstof.fuelDescription) {
        "Elektriciteit" -> BEVCar(
            id = id, userId = userId,
            licensePlate = licensePlate,
            brand = brand, model = model,
            productionYear = productionYear, trim = trim, color = color,
            seats = seats, doors = doors, vehicleType = vehicleType,
            readyToDriveWeightKg = readyToDriveWeightKg,
            consumptionCombined = combinedConsumption,
            co2EmissionCombined = co2EmissionCombined,
            roadTaxPerQuarter = roadTaxPerQuarter,
            batteryCapacityKWh = batteryCapacityKWh
                ?: throw UnprocessableException("batteryCapacityKWh is verplicht voor een BEV")
        )
        "Waterstof" -> FCEVCar(
            id = id, userId = userId,
            licensePlate = licensePlate,
            brand = brand, model = model,
            productionYear = productionYear, trim = trim, color = color,
            seats = seats, doors = doors, vehicleType = vehicleType,
            readyToDriveWeightKg = readyToDriveWeightKg,
            consumptionCombined = combinedConsumption,
            co2EmissionCombined = co2EmissionCombined,
            roadTaxPerQuarter = roadTaxPerQuarter,
            tankCapacityKgH2 = tankCapacityKgH2
                ?: throw UnprocessableException("tankCapacityKgH2 is verplicht voor een FCEV")
        )
        "Benzine", "Diesel", "LPG" -> ICECar(
            id = id, userId = userId,
            licensePlate = licensePlate,
            brand = brand, model = model,
            productionYear = productionYear, trim = trim, color = color,
            seats = seats, doors = doors, vehicleType = vehicleType,
            readyToDriveWeightKg = readyToDriveWeightKg,
            consumptionCombined = combinedConsumption,
            co2EmissionCombined = co2EmissionCombined,
            roadTaxPerQuarter = roadTaxPerQuarter,
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
