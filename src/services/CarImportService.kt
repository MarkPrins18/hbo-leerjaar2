package services

import clients.RdwClient
import mappers.RdwCarMapper
import mappers.RdwMappingException
import models.Car
import repositories.CarRepository
import requests.CarRequest


class CarImportService(
    private val rdwClient: RdwClient,
    private val carRepository: CarRepository
) {
    suspend fun importCar(request: CarRequest): Car {
        val voertuig = rdwClient.getVehicle(request.licensePlate)
            ?: throw RdwMappingException(
                "Geen voertuig gevonden voor kenteken ${request.licensePlate}"
            )

        val brandstoffen = rdwClient.getFuel(request.licensePlate)

        val car = RdwCarMapper.toCar(
            voertuig = voertuig,
            brandstoffen = brandstoffen,
            id = 0,
            ownerId = request.ownerId,
            trim = request.trim,
            tankCapacityL = request.tankCapacityL,
            automaticTransmission = request.automaticTransmission,
            batteryCapacityKWh = request.batteryCapacityKWh,
            tankCapacityKgH2 = request.tankCapacityKgH2
        )
        return carRepository.createCar(car)
    }
}