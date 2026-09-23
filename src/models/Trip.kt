package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

// Issue #11 - Ritregistratie

/**
 * Een rit hoort bij een (bevestigde) reservering. `endTime`/`endLatitude`/
 * `endLongitude`/`distanceKm` zijn null zolang de rit nog loopt en worden pas
 * gezet als de rit wordt afgerond (zie TripRepository.endTrip).
 */
@Serializable
data class Trip(
    val id: Int? = null,
    val reservationId: Int,
    @Serializable(with = InstantMillisSerializer::class)
    val startTime: Instant,
    @Serializable(with = InstantMillisSerializer::class)
    val endTime: Instant? = null,
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double? = null,
    val endLongitude: Double? = null,
    val distanceKm: Double? = null
)
