package services

import clients.GeminiClient
import clients.RdwClient
import mappers.toCar
import repositories.CarRepository
import dto.*
import models.*
import config.PhotoStorage
import repositories.LocationRepository
import repositories.PhotoRepository
import repositories.RentalTermsRepository
import repositories.UserRepository
import responses.FuelCostResponse
import responses.TcoResponse
import kotlin.coroutines.cancellation.CancellationException

class CarService(
    private val rdwClient: RdwClient,
    private val geminiClient: GeminiClient,
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository,
    private val photoRepository: PhotoRepository,
    private val photoStorage: PhotoStorage,
    private val rentalTermsRepository: RentalTermsRepository
) {
    suspend fun importCar(request: CarDto, userId: Int): Car {
        request.validate()
        checkLicensePlateAvailable(request.licensePlate)
        checkUserExists(userId)

        val voertuig = callRdw { rdwClient.getVehicle(request.licensePlate) }
            ?: throw UnprocessableException("Geen voertuig gevonden bij de RDW voor kenteken ${request.licensePlate}")

        val brandstoffen = callRdw { rdwClient.getFuel(request.licensePlate) }

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

    suspend fun updateCar(carId: Int, userId: Int, change: CarChangeDto): Car {
        val car = getOwnedCar(carId, userId)

        change.validate(car)
        change.licensePlate?.let { checkLicensePlateAvailable(it, carId) }

        return carRepository.updateCar(carId, change)
            ?: throw carNotFound(carId)
    }

    /** Asks Gemini for the cost values that are still missing on the car and stores them. */
    suspend fun fillMissingCostData(carId: Int, userId: Int): Car {
        val car = getOwnedCar(carId, userId)

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

    /** A missing consumption is first filled in by Gemini; if it is still missing, the cost cannot be calculated. */
    suspend fun getFuelCost(carId: Int, userId: Int, distanceKm: Double): FuelCostResponse {
        val car = getOwnedCar(carId, userId)

        validateDistanceKm(distanceKm)

        val carWithConsumption = if (car.consumptionCombined == null) fillMissingCostData(carId, userId) else car
        return calculateFuelCost(carWithConsumption, distanceKm)
    }

    /** Missing consumption or road tax is first filled in by Gemini, just like for the fuel cost. */
    suspend fun getTco(carId: Int, userId: Int, annualKm: Double): TcoResponse {
        val car = getOwnedCar(carId, userId)

        validateAnnualKm(annualKm)

        val completeCar =
            if (car.consumptionCombined == null || car.roadTaxPerQuarter == null) fillMissingCostData(carId, userId) else car
        return calculateTco(completeCar, annualKm)
    }

    suspend fun getCarByLicensePlate(licensePlate: String): Car =
        carRepository.getCarByLicensePlate(licensePlate)
            ?: throw ResourceNotFoundException("Auto met kenteken $licensePlate niet gevonden")

    suspend fun deleteCar(carId: Int, userId: Int) {
        getOwnedCar(carId, userId)

        val photoFileNames = photoRepository.getFileNamesByCarId(carId)

        if (!carRepository.deleteCar(carId)) {
            throw carNotFound(carId)
        }

        photoFileNames.forEach(photoStorage::delete)
    }

    suspend fun getCars(filter: CarFilter): List<Car> {
        filter.validate()

        val cars = carRepository.getAllCars()
        if (filter.maxDistanceKm == null && filter.maxPrice == null) return cars

        return cars.filter { car ->
            val location = locationRepository.getLatestLocationByCarId(car.id)
            val rentalTerms = rentalTermsRepository.getRentalTermsByCarId(car.id)
            filter.matches(location, rentalTerms)
        }
    }

    private fun carNotFound(carId: Int) = ResourceNotFoundException("Auto $carId niet gevonden")

    private suspend fun getOwnedCar(carId: Int, userId: Int): Car {
        val car = carRepository.getCarById(carId)
            ?: throw carNotFound(carId)

        if (car.userId != userId) throw ForbiddenException()
        return car
    }

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
