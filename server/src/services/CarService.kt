package services

import clients.RdwClient
import io.ktor.server.plugins.NotFoundException
import mappers.RdwCarMapper
import repositories.CarRepository
import dto.*
import models.*
import repositories.LocationRepository
import kotlin.coroutines.cancellation.CancellationException

class CarService(
    private val rdwClient: RdwClient,
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository
) {
    suspend fun importCar(request: CarDto): Car {
        request.validate()

        if (carRepository.getCarByLicensePlate(request.licensePlate) != null) {
            throw ConflictException("Kenteken ${request.licensePlate} bestaat al")
        }

        val voertuig = callRdw { rdwClient.getVehicle(request.licensePlate) }
            ?: throw UnprocessableException("Geen voertuig gevonden bij de RDW voor kenteken ${request.licensePlate}")

        val brandstoffen = callRdw { rdwClient.getFuel(request.licensePlate) }

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

    suspend fun updateCar(carId: Int, change: CarChangeDto): Car {
        val car = carRepository.getCarById(carId)
            ?: throw carNotFound(carId)

        change.validate(car)

        return carRepository.updateCar(carId, change)
            ?: throw carNotFound(carId)
    }

    suspend fun getCarByLicensePlate(licensePlate: String): Car =
        carRepository.getCarByLicensePlate(licensePlate)
            ?: throw NotFoundException("Auto met kenteken $licensePlate niet gevonden")

    suspend fun deleteCar(carId: Int) {
        if (!carRepository.deleteCar(carId)) {
            throw carNotFound(carId)
        }
    }

    suspend fun getCars(filter: CarFilter): List<Car> {
        filter.validate()

        val cars = carRepository.getAllCars()
        if (filter.maxDistanceKm == null) return cars // add `&& filter.maxPrice == null` once matchesPrice is enabled

        return cars.filter { car ->
            val location = locationRepository.getLatestLocationByCarId(car.id)
            filter.matches(location)
        }
    }

    private fun carNotFound(carId: Int) = NotFoundException("Auto $carId niet gevonden")

    private suspend fun <T> callRdw(block: suspend () -> T): T =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            throw ExternalServiceException("De RDW API is niet bereikbaar of gaf een ongeldig antwoord", exception)
        }
}
