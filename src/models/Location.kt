package models

import kotlin.time.Instant
//geen import kotlinx.serialization.Serializable

data class Location(
    val id: Int,
    val carId: Int,
    val userId:Int? = null,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Instant
)