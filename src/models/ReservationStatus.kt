package models

import kotlinx.serialization.Serializable

@Serializable
enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}