package services

import dto.RentalTermsRequest
import models.RentalTerms
import repositories.CarRepository
import repositories.RentalTermsRepository

class RentalTermsService(
    private val rentalTermsRepository: RentalTermsRepository,
    private val carRepository: CarRepository
) {
    suspend fun getRentalTerms(carId: Int): RentalTerms {
        if (!rentalTermsRepository.carExists(carId)) {
            throw carNotFound(carId)
        }
        return rentalTermsRepository.getRentalTermsByCarId(carId)
            ?: throw ResourceNotFoundException("Geen verhuurvoorwaarden bekend voor auto $carId")
    }

    suspend fun setRentalTerms(carId: Int, userId: Int, request: RentalTermsRequest): RentalTerms {
        val car = carRepository.getCarById(carId)
            ?: throw carNotFound(carId)

        if (car.userId != userId) throw ForbiddenException()

        request.validate()

        return rentalTermsRepository.saveRentalTerms(carId, request)
    }

    private fun carNotFound(carId: Int) = ResourceNotFoundException("Auto $carId niet gevonden")
}
