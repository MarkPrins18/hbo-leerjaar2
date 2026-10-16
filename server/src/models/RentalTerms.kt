package models

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class RentalTerms(
    val id: Int,
    val carId: Int,
    val pricePerDay: Double,
    val pickupLocation: String,
    val pickupTimeStart: LocalTime? = null,
    val pickupTimeEnd: LocalTime? = null,
    val returnLocation: String? = null,
    val returnTimeStart: LocalTime? = null,
    val returnTimeEnd: LocalTime? = null
)
