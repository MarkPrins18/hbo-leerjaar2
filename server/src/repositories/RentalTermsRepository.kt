package repositories

import shared.requests.RentalTermsRequest
import shared.models.RentalTerms

interface RentalTermsRepository {
    suspend fun carExists(carId: Int): Boolean
    suspend fun getRentalTermsByCarId(carId: Int): RentalTerms?
    suspend fun saveRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms
}
