package services

import contracts.requests.RentalTermsRequest
import contracts.models.RentalTerms
import repositories.CarRepository
import repositories.RentalTermsRepository

class RentalTermsService(
    private val rentalTermsRepository: RentalTermsRepository,
    private val carRepository: CarRepository
) {
    suspend fun getRentalTerms(carId: Int): RentalTerms {
        carRepository.getCarById(carId) ?: throw carNotFound(carId)
        return rentalTermsRepository.getRentalTermsByCarId(carId)
            ?: throw ResourceNotFoundException("Geen verhuurvoorwaarden bekend voor auto $carId")
    }

    suspend fun setRentalTerms(carId: Int, userId: Int, request: RentalTermsRequest): RentalTerms {
        carRepository.getOwnedCar(carId, userId)

        request.validate()

        return rentalTermsRepository.saveRentalTerms(carId, request)
    }
}
