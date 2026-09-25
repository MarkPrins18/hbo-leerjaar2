package mappers

import models.BEVCar
import models.Car
import models.FCEVCar
import models.FuelType
import models.ICECar
import models.VehicleType
import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto

class RdwMappingException(message: String) : Exception(message)

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
        val hoofdbrandstof = brandstoffen.firstOrNull { it.fuelSequenceNumber == "1" }
            ?: throw RdwMappingException("Geen hoofdbrandstof gevonden voor kenteken ${voertuig.licensePlate}")

        val vehicleType = mapVehicleType(voertuig.vehicleType)

        val readyToDriveWeightKg = voertuig.readyToDriveWeightKg?.toIntOrNull()
            ?: throw RdwMappingException("Geen massa rijklaar bekend voor kenteken ${voertuig.licensePlate}")

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
                    ?: throw RdwMappingException("batteryCapacityKWh is verplicht voor een BEV")
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
                    ?: throw RdwMappingException("tankCapacityKgH2 is verplicht voor een FCEV")
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
                    ?: throw RdwMappingException("tankCapacityL is verplicht voor een ICE-auto"),
                automaticTransmission = automaticTransmission
                    ?: throw RdwMappingException("automaticTransmission is verplicht voor een ICE-auto")
            )
            else -> throw RdwMappingException("Niet-ondersteunde brandstof: $fuelDescription")
        }
    }

    private fun mapVehicleType(voertuigsoort: String): VehicleType = when (voertuigsoort) {
        "Personenauto" -> VehicleType.PASSENGER_CAR
        "Bedrijfsauto" -> VehicleType.LIGHT_COMMERCIAL_VEHICLE
        else -> throw RdwMappingException("Onbekend voertuigsoort: $voertuigsoort")
    }

    private fun mapFuelType(fuelDescription: String): FuelType = when (fuelDescription) {
        "Benzine" -> FuelType.PETROL
        "Diesel" -> FuelType.DIESEL
        "LPG" -> FuelType.LPG
        else -> throw RdwMappingException("Niet-ondersteunde brandstof: $fuelDescription")
    }

    private fun parseProductionYear(firstAdmissionDate: String): Int {
        if (firstAdmissionDate.length < 4) {
            throw RdwMappingException("Onbekend datumformaat voor datum_eerste_toelating: $firstAdmissionDate")
        }
        return firstAdmissionDate.take(4).toIntOrNull()
            ?: throw RdwMappingException("Kon jaartal niet parsen uit datum_eerste_toelating: $firstAdmissionDate")
    }
}