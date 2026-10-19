package dto

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class RentalTermsRequest(
    val pricePerDay: Double,
    val pickupLocation: String,
    val pickupTimeStart: LocalTime? = null,
    val pickupTimeEnd: LocalTime? = null,
    val returnLocation: String? = null,
    val returnTimeStart: LocalTime? = null,
    val returnTimeEnd: LocalTime? = null
)
