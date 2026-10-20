package services

import models.Car
import repositories.CarRepository
import kotlin.coroutines.cancellation.CancellationException

internal fun carNotFound(carId: Int) = ResourceNotFoundException("Auto $carId niet gevonden")

internal suspend fun CarRepository.getOwnedCar(carId: Int, userId: Int): Car {
    val car = getCarById(carId)
        ?: throw carNotFound(carId)

    if (car.userId != userId) throw ForbiddenException()
    return car
}

/** [carId] is the car being changed; it may keep its own license plate. */
internal suspend fun CarRepository.checkLicensePlateAvailable(licensePlate: String, carId: Int? = null) {
    val existingCar = getCarByLicensePlate(licensePlate)
    if (existingCar != null && existingCar.id != carId) {
        throw ConflictException("Kenteken $licensePlate bestaat al")
    }
}

internal suspend fun <T> callExternal(service: String, block: suspend () -> T): T =
    try {
        block()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        throw ExternalServiceException("De $service API is niet bereikbaar of gaf een ongeldig antwoord", exception)
    }
