package repositories

import dto.RentalTermsRequest
import mappers.toRentalTerms
import models.RentalTerms
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import tables.CarTable
import tables.RentalTermsTable

class ExposedRentalTermsRepository : RentalTermsRepository {

    override suspend fun carExists(carId: Int): Boolean = suspendTransaction {
        !CarTable.selectAll().where { CarTable.id eq carId }.empty()
    }

    override suspend fun getRentalTermsByCarId(carId: Int): RentalTerms? = suspendTransaction {
        RentalTermsTable.selectAll().where { RentalTermsTable.carId eq carId }.singleOrNull()?.toRentalTerms()
    }

    override suspend fun saveRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms = suspendTransaction {
        val exists = !RentalTermsTable.selectAll().where { RentalTermsTable.carId eq carId }.empty()
        if (exists) {
            RentalTermsTable.update({ RentalTermsTable.carId eq carId }) {
                it.setRentalTerms(request)
            }
        } else {
            RentalTermsTable.insert {
                it[RentalTermsTable.carId] = carId
                it.setRentalTerms(request)
            }
        }
        RentalTermsTable.selectAll().where { RentalTermsTable.carId eq carId }.single().toRentalTerms()
    }

    private fun UpdateBuilder<*>.setRentalTerms(request: RentalTermsRequest) {
        this[RentalTermsTable.pricePerDay] = request.pricePerDay.toBigDecimal()
        this[RentalTermsTable.pickupLocation] = request.pickupLocation
        this[RentalTermsTable.pickupTimeStart] = request.pickupTimeStart
        this[RentalTermsTable.pickupTimeEnd] = request.pickupTimeEnd
        this[RentalTermsTable.returnLocation] = request.returnLocation
        this[RentalTermsTable.returnTimeStart] = request.returnTimeStart
        this[RentalTermsTable.returnTimeEnd] = request.returnTimeEnd
    }
}
