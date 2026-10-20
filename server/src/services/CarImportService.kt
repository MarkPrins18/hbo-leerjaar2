package services

import clients.VehicleRegistry
import dto.CarDto
import mappers.toCar
import models.Car
import repositories.CarRepository
import repositories.UserRepository

class CarImportService(
    private val vehicleRegistry: VehicleRegistry,
    private val carRepository: CarRepository,
    private val userRepository: UserRepository
) {
    suspend fun importCar(request: CarDto, userId: Int): Car {
        request.validate()
        carRepository.checkLicensePlateAvailable(request.licensePlate)
        checkUserExists(userId)

        val voertuig = callExternal("RDW") { vehicleRegistry.getVehicle(request.licensePlate) }
            ?: throw UnprocessableException("Geen voertuig gevonden bij de RDW voor kenteken ${request.licensePlate}")

        val brandstoffen = callExternal("RDW") { vehicleRegistry.getFuel(request.licensePlate) }

        val car = voertuig.toCar(
            brandstoffen = brandstoffen,
            id = 0,
            userId = userId,
            trim = request.trim,
            tankCapacityL = request.tankCapacityL,
            automaticTransmission = request.automaticTransmission,
            batteryCapacityKWh = request.batteryCapacityKWh,
            tankCapacityKgH2 = request.tankCapacityKgH2,
            consumptionCombined = request.consumptionCombined,
            roadTaxPerQuarter = request.roadTaxPerQuarter
        )
        car.validateRdwData()
        return carRepository.createCar(car)
    }

    private suspend fun checkUserExists(userId: Int) {
        if (!userRepository.userExists(userId)) {
            throw ResourceNotFoundException("Gebruiker $userId niet gevonden")
        }
    }
}
