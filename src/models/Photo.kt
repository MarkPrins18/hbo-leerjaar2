package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

//fix: @Serializable stond hier niet -- zonder deze annotatie kan Photo niet
//via call.receive<Photo>()/call.respond(photo) verstuurd worden.
@Serializable
data class Photo(
    val id: Int? = null, // fix: Int (was niet-nullable) zodat id net als bij Car genegeerd kan worden bij aanmaken
    val carId: Int,
    val url: String,
    @Serializable(with = InstantMillisSerializer::class)
    val uploadedAt: Instant
)
