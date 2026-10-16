package services

import clients.GeminiClient
import clients.RdwClient
import mappers.RdwCarMapper
import repositories.CarRepository
import dto.*
import models.*
import repositories.LocationRepository
import repositories.UserRepository
import kotlin.coroutines.cancellation.CancellationException

class CarService(
    private val rdwClient: RdwClient,
    private val geminiClient: GeminiClient,
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository
) {
    suspend fun importCar(request: CarDto): Car {
        request.validate()
        checkLicensePlateAvailable(request.licensePlate)
        checkUserExists(request.userId)

        val voertuig = callRdw { rdwClient.getVehicle(request.licensePlate) }
            ?: throw UnprocessableException("Geen voertuig gevonden bij de RDW voor kenteken ${request.licensePlate}")

        val brandstoffen = callRdw { rdwClient.getFuel(request.licensePlate) }

        val car = RdwCarMapper.toCar(
            voertuig = voertuig,
            brandstoffen = brandstoffen,
            id = 0,
            userId = request.userId,
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

    suspend fun updateCar(carId: Int, change: CarChangeDto): Car {
        val car = carRepository.getCarById(carId)
            ?: throw carNotFound(carId)

        change.validate(car)
        change.licensePlate?.let { checkLicensePlateAvailable(it, carId) }
        change.userId?.let { checkUserExists(it) }

        return carRepository.updateCar(carId, change)
            ?: throw carNotFound(carId)
    }

    /** Asks Gemini for the cost values that are still missing on the car and stores them. */
    suspend fun fillMissingCostData(carId: Int): Car {
        val car = carRepository.getCarById(carId)
            ?: throw carNotFound(carId)

        if (car.roadTaxPerQuarter != null && car.consumptionCombined != null) return car

        val estimate = callGemini { geminiClient.estimateCarCosts(car) }
        estimate.validate()

        val change = CarChangeDto(
            roadTaxPerQuarter = estimate.roadTaxPerQuarter.takeIf { car.roadTaxPerQuarter == null },
            consumptionCombined = estimate.consumptionCombined.takeIf { car.consumptionCombined == null }
        )
        return carRepository.updateCar(carId, change)
            ?: throw carNotFound(carId)
    }

    suspend fun getCarByLicensePlate(licensePlate: String): Car =
        carRepository.getCarByLicensePlate(licensePlate)
            ?: throw ResourceNotFoundException("Auto met kenteken $licensePlate niet gevonden")

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

    private fun carNotFound(carId: Int) = ResourceNotFoundException("Auto $carId niet gevonden")

    /** [carId] is the car being changed; it may keep its own license plate. */
    private suspend fun checkLicensePlateAvailable(licensePlate: String, carId: Int? = null) {
        val existingCar = carRepository.getCarByLicensePlate(licensePlate)
        if (existingCar != null && existingCar.id != carId) {
            throw ConflictException("Kenteken $licensePlate bestaat al")
        }
    }

    private suspend fun checkUserExists(userId: Int) {
        if (!userRepository.userExists(userId)) {
            throw ResourceNotFoundException("Gebruiker $userId niet gevonden")
        }
    }

    private suspend fun <T> callRdw(block: suspend () -> T): T =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            throw ExternalServiceException("De RDW API is niet bereikbaar of gaf een ongeldig antwoord", exception)
        }

    private suspend fun <T> callGemini(block: suspend () -> T): T =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            throw ExternalServiceException("De Gemini API is niet bereikbaar of gaf een ongeldig antwoord", exception)
        }
}
