package services

import dto.RentalTermsRequest
import models.RentalTerms
import repositories.RentalTermsRepository

class RentalTermsService(
    private val rentalTermsRepository: RentalTermsRepository
) {
    suspend fun getRentalTerms(carId: Int): RentalTerms {
        if (!rentalTermsRepository.carExists(carId)) {
            throw carNotFound(carId)
        }
        return rentalTermsRepository.getRentalTermsByCarId(carId)
            ?: throw ResourceNotFoundException("Geen verhuurvoorwaarden bekend voor auto $carId")
    }

    suspend fun setRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms {
        request.validate()

        if (!rentalTermsRepository.carExists(carId)) {
            throw carNotFound(carId)
        }
        return rentalTermsRepository.saveRentalTerms(carId, request)
    }

    private fun carNotFound(carId: Int) = ResourceNotFoundException("Auto $carId niet gevonden")
}
