package services

import repositories.CarRepository
import contracts.requests.*
import contracts.models.*
import config.PhotoStorage
import repositories.LocationRepository
import repositories.PhotoRepository
import repositories.RentalTermsRepository
import repositories.ReservationRepository
import contracts.responses.CarMapResponse

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
            carRepository.getCarById(carId) ?: throw carNotFound(carId)
            throw ConflictException("Auto $carId heeft nog een actieve reservering of lopende rit en kan niet worden verwijderd")
        }

        photoFileNames.forEach(photoStorage::delete)
    }

    suspend fun getCars(filter: CarFilter): List<Car> {
        filter.validate()

        val cars = getAvailableCars(filter)
        if (filter.maxDistanceKm == null && filter.maxPrice == null) return cars

        val locationsByCarId = locationRepository.getLatestLocationsByCarId()
        val rentalTermsByCarId = getRentalTermsByCarId()
        return cars.filter { car -> filter.matches(locationsByCarId[car.id], rentalTermsByCarId[car.id]) }
    }

    /** One query for all latest locations and one for all rental terms, not one per car. */
    suspend fun getCarMap(filter: CarFilter): List<CarMapResponse> {
        filter.validate()

        val cars = getAvailableCars(filter)
        val locationsByCarId = locationRepository.getLatestLocationsByCarId()
        val rentalTermsByCarId = getRentalTermsByCarId()
        return cars.mapNotNull { car ->
            val location = locationsByCarId[car.id]
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
