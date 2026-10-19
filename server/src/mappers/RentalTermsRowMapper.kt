package mappers

import models.RentalTerms
import org.jetbrains.exposed.v1.core.ResultRow
import tables.RentalTermsTable

fun ResultRow.toRentalTerms(): RentalTerms = RentalTerms(
    id = this[RentalTermsTable.id],
    carId = this[RentalTermsTable.carId],
    pricePerDay = this[RentalTermsTable.pricePerDay].toDouble(),
    pickupLocation = this[RentalTermsTable.pickupLocation],
    pickupTimeStart = this[RentalTermsTable.pickupTimeStart],
    pickupTimeEnd = this[RentalTermsTable.pickupTimeEnd],
    returnLocation = this[RentalTermsTable.returnLocation],
    returnTimeStart = this[RentalTermsTable.returnTimeStart],
    returnTimeEnd = this[RentalTermsTable.returnTimeEnd]
)
