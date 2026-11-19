package services

import kotlin.time.Instant

data class CarFilter(
    val maxPrice: Double? = null,
    val maxDistanceKm: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
)