package services

import repositories.CarRepository
import dto.*
import models.*
import config.PhotoStorage
import repositories.LocationRepository
import repositories.PhotoRepository
import repositories.RentalTermsRepository

class CarService(
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository,
    private val photoRepository: PhotoRepository,
    private val photoStorage: PhotoStorage,
    private val rentalTermsRepository: RentalTermsRepository
) {
    suspend fun updateCar(carId: Int, userId: Int, change: CarChangeDto): Car {
        val car = carRepository.getOwnedCar(carId, userId)

        change.validate(car)
        change.licensePlate?.let { carRepository.checkLicensePlateAvailable(it, carId) }

        return carRepository.updateCar(carId, change)
            ?: throw carNotFound(carId)
    }

    suspend fun getCarById(carId: Int): Car =
        carRepository.getCarById(carId) ?: throw carNotFound(carId)

    suspend fun getCarByLicensePlate(licensePlate: String): Car =
        carRepository.getCarByLicensePlate(licensePlate)
            ?: throw ResourceNotFoundException("Auto met kenteken $licensePlate niet gevonden")

    suspend fun deleteCar(carId: Int, userId: Int) {
        carRepository.getOwnedCar(carId, userId)

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
}
