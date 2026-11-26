package repositories

import contracts.requests.RentalTermsRequest
import contracts.models.RentalTerms

interface RentalTermsRepository {
    suspend fun getRentalTermsByCarId(carId: Int): RentalTerms?
    suspend fun getAllRentalTerms(): List<RentalTerms>
    suspend fun saveRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms
}
