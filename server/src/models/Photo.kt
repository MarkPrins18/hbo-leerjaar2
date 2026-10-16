package models

import kotlin.time.Instant

//geen import kotlinx.serialization.Serializable

data class Photo(
    val id: Int,
    val carId: Int,
    val url: String, //?
    val uploadedAt: Instant
)