package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Photo(
    val id: Int,
    val carId: Int,
    val url: String,
    val uploadedAt: Instant
)
