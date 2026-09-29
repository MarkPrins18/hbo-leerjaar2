package services

import clients.RdwClient
import io.ktor.http.HttpStatusCode
import mappers.RdwCarMapper
import mappers.RdwMappingException
import repositories.CarRepository
import requests.*
import models.*

class CarService(
    private val rdwClient: RdwClient,
    private val carRepository: CarRepository
) {
    suspend fun importCar(request: CarRequest): Car { //rename || suspendtransaction
        val voertuig = rdwClient.getVehicle(request.licensePlate)
            ?: throw ApiException(HttpStatusCode.BadRequest,
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
        return carRepository.createCar(car) //is return logical?
    }

    suspend fun changeCar(carId: Int, change: CarChangeRequest) : Car? { //suspendTransaction || rename
        val car = carRepository.getCarById(carId)
            ?: return null

        when (car) {
            is ICECar -> {
                if (change.batteryCapacityKWh != null ||
                    change.tankCapacityKgH2 != null
                ) {
                    throw ApiException(HttpStatusCode.BadRequest, "Deze velden kunnen niet worden aangepast voor een ICE-auto")
                }
            }

            is BEVCar -> {
                if (change.fuelType != null ||
                    change.tankCapacityL != null ||
                    change.automaticTransmission != null ||
                    change.tankCapacityKgH2 != null
                ) {
                    throw ApiException(HttpStatusCode.BadRequest, "Deze velden kunnen niet worden aangepast voor een BEV-auto")
                }
            }

            is FCEVCar -> {
                if (change.fuelType != null ||
                    change.tankCapacityL != null ||
                    change.automaticTransmission != null ||
                    change.batteryCapacityKWh != null
                ) {
                    throw ApiException(HttpStatusCode.BadRequest, "Deze velden kunnen niet worden aangepast voor een FCEV-auto")
                }
            }
        }

        return carRepository.updateCar(carId, change)
    }
}