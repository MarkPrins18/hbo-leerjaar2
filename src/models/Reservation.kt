package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

// Issue #9 - Reservering-en-huurflow

@Serializable
enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

/**
 * Net als bij Car: het `id`-veld wordt genegeerd bij het aanmaken (de repository
 * genereert er zelf een), en `status`/`createdAt` worden bij het aanmaken altijd
 * door de repository op respectievelijk PENDING en "nu" gezet, ongeacht wat de
 * client meestuurt -- die velden mag een client niet zelf bepalen.
 */
@Serializable
data class Reservation(
    val id: Int? = null,
    val carId: Int,
    val renterId: Int,
    @Serializable(with = InstantMillisSerializer::class)
    val startTime: Instant,
    @Serializable(with = InstantMillisSerializer::class)
    val endTime: Instant,
    val status: ReservationStatus = ReservationStatus.PENDING,
    @Serializable(with = InstantMillisSerializer::class)
    val createdAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
)


