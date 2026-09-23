package models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

//fix: @Serializable stond hier niet -- zonder deze annotatie kan Location niet
//via call.receive<Location>()/call.respond(location) verstuurd worden.
@Serializable
data class Location(
    val id: Int? = null, // fix: Int (was niet-nullable) zodat id net als bij Car genegeerd kan worden bij aanmaken
    val carId: Int,
    val userId: Int? = null,
    val latitude: Double,
    val longitude: Double,
    @Serializable(with = InstantMillisSerializer::class)
    val timestamp: Instant
)
