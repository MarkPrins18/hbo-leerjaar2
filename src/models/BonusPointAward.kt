package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

// Issue #12 - Bonuspuntensysteem

/** Eén toekenning van bonuspunten aan een huurder voor een afgeronde rit. */
@Serializable
data class BonusPointAward(
    val id: Int? = null,
    val renterId: Int,
    val tripId: Int,
    val points: Int,
    @Serializable(with = InstantMillisSerializer::class)
    val awardedAt: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
)
