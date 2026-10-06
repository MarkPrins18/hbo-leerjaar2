package mappers

import models.*
import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto
import services.UnprocessableException

object RdwCarMapper {

    fun toCar(
        voertuig: RdwVehicleDto,
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
                ?: throw UnprocessableException("Geen niet-elektrische brandstof gevonden voor hybride kenteken ${voertuig.licensePlate}")
        } else {
            // Not a hybrid: just take the main fuel at sequence number 1
            brandstoffen.firstOrNull { it.fuelSequenceNumber == "1" }
                ?: throw UnprocessableException("Geen hoofdbrandstof gevonden voor kenteken ${voertuig.licensePlate}")
        }

        val vehicleType = mapVehicleType(voertuig.vehicleType)

        val readyToDriveWeightKg = voertuig.readyToDriveWeightKg?.toIntOrNull()
            ?: throw UnprocessableException("Geen massa rijklaar bekend voor kenteken ${voertuig.licensePlate}")

        val consumptionCombined = hoofdbrandstof.consumptionCombined?.toDoubleOrNull() //No throw!!
        val co2EmissionCombined = hoofdbrandstof.co2EmissionCombined?.toDoubleOrNull()

        val productionYear = parseProductionYear(voertuig.firstAdmissionDate)
        val seats = voertuig.seats?.toIntOrNull()
        val doors = voertuig.doors?.toIntOrNull()

        return when (val fuelDescription = hoofdbrandstof.fuelDescription) {
            "Elektriciteit" -> BEVCar(
                id = id, ownerId = ownerId,
                licensePlate = voertuig.licensePlate,
                brand = voertuig.brand, model = voertuig.model,
                productionYear = productionYear, trim = trim, color = voertuig.color,
                seats = seats, doors = doors, vehicleType = vehicleType,
                readyToDriveWeightKg = readyToDriveWeightKg,
                consumptionCombined = consumptionCombined,
                co2EmissionCombined = co2EmissionCombined,
                batteryCapacityKWh = batteryCapacityKWh
                    ?: throw UnprocessableException("batteryCapacityKWh is verplicht voor een BEV")
            )
            "Waterstof" -> FCEVCar(
                id = id, ownerId = ownerId,
                licensePlate = voertuig.licensePlate,
                brand = voertuig.brand, model = voertuig.model,
                productionYear = productionYear, trim = trim, color = voertuig.color,
                seats = seats, doors = doors, vehicleType = vehicleType,
                readyToDriveWeightKg = readyToDriveWeightKg,
                consumptionCombined = consumptionCombined,
                co2EmissionCombined = co2EmissionCombined,
                tankCapacityKgH2 = tankCapacityKgH2
                    ?: throw UnprocessableException("tankCapacityKgH2 is verplicht voor een FCEV")
            )
            "Benzine", "Diesel", "LPG" -> ICECar(
                id = id, ownerId = ownerId,
                licensePlate = voertuig.licensePlate,
                brand = voertuig.brand, model = voertuig.model,
                productionYear = productionYear, trim = trim, color = voertuig.color,
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
}