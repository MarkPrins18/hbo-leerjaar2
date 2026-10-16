package models

import kotlin.time.Instant
//geen import kotlinx.serialization.Serializable

/**
 * userId the driver, only set during an active ride
 * rideId the ride this position belongs to, only set during an active ride
 * speed the speed in meters per second, if known
 */
data class Location(
    val id: Int,
    val carId: Int,
    val userId: Int? = null,
    val rideId: Int? = null,
    val latitude: Double,
    val longitude: Double,
    val speed: Double?,
    val timestamp: Instant
)