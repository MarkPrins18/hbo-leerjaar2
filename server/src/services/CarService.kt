package services

import repositories.CarRepository
import shared.requests.*
import shared.models.*
import config.PhotoStorage
import repositories.LocationRepository
import repositories.PhotoRepository
import repositories.RentalTermsRepository
import repositories.ReservationRepository
import shared.responses.CarMapResponse

class CarService(
    private val carRepository: CarRepository,
    private val locationRepository: LocationRepository,
    private val photoRepository: PhotoRepository,
    private val photoStorage: PhotoStorage,
    private val rentalTermsRepository: RentalTermsRepository,
    private val reservationRepository: ReservationRepository
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

    suspend fun getCarsByOwner(userId: Int): List<Car> = carRepository.getCarsByUserId(userId)

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

        val cars = getAvailableCars(filter)
        if (filter.maxDistanceKm == null && filter.maxPrice == null) return cars

        val rentalTermsByCarId = getRentalTermsByCarId()
        return cars.filter { car ->
            val location = locationRepository.getLatestLocationByCarId(car.id)
            filter.matches(location, rentalTermsByCarId[car.id])
        }
    }

    /** One query for all rental terms; the latest location is still fetched per car. */
    suspend fun getCarMap(filter: CarFilter): List<CarMapResponse> {
        filter.validate()

        val cars = getAvailableCars(filter)
        val rentalTermsByCarId = getRentalTermsByCarId()
        return cars.mapNotNull { car ->
            val location = locationRepository.getLatestLocationByCarId(car.id)
            val rentalTerms = rentalTermsByCarId[car.id]
            if (!filter.matches(location, rentalTerms)) return@mapNotNull null

            CarMapResponse(car, location?.latitude, location?.longitude, rentalTerms?.pricePerDay)
        }
    }

    private suspend fun getRentalTermsByCarId(): Map<Int, RentalTerms> =
        rentalTermsRepository.getAllRentalTerms().associateBy { it.carId }

    private suspend fun getAvailableCars(filter: CarFilter): List<Car> {
        val cars = carRepository.getAllCars()
        if (filter.startTime == null || filter.endTime == null) return cars

        val reservedCarIds = reservationRepository.getReservedCarIds(filter.startTime, filter.endTime)
        return cars.filter { it.id !in reservedCarIds }
    }
}
