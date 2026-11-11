package repositories

import shared.models.Car
import shared.requests.CarChangeDto

interface CarRepository {
    suspend fun getAllCars(): List<Car>

    suspend fun getCarByLicensePlate(licensePlate: String): Car?

    suspend fun getCarById(carId: Int): Car?

    suspend fun getCarsByUserId(userId: Int): List<Car>

    /**
     * Saves a new car. The `id` field of [car] is ignored; the repository
     * generates a new id and returns the saved car including that id.
     */
    suspend fun createCar(car: Car): Car

    /** Returns the updated car, or null if no car exists with id [carId]. */
    suspend fun updateCar(carId: Int, change: CarChangeDto): Car?

    suspend fun deleteCar(carId: Int): Boolean
}