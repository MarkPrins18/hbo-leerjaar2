package models

import kotlinx.serialization.Serializable

// Issue #10 - Route naar auto / externe routering

@Serializable
data class RouteResult(
    val distanceMeters: Double,
    val durationSeconds: Double
)
