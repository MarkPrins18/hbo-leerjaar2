package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import models.Trip
import repositories.TripRepository

// Issue #11 - Ritregistratie

@Serializable
data class EndTripRequest(
    val endLatitude: Double,
    val endLongitude: Double,
    val distanceKm: Double
)

fun Route.tripRoutes(repository: TripRepository) {
    route("/trips") {
        get {
            call.respond(repository.getAllTrips())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig id")
                return@get
            }
            val trip = repository.getTripById(id)
            if (trip == null) call.respond(HttpStatusCode.NotFound) else call.respond(trip)
        }
        post {
            val trip = call.receive<Trip>()
            call.respond(HttpStatusCode.Created, repository.startTrip(trip))
        }
        patch("/{id}/end") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig id")
                return@patch
            }
            val body = call.receive<EndTripRequest>()
            val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            val ended = repository.endTrip(id, now, body.endLatitude, body.endLongitude, body.distanceKm)
            if (ended == null) call.respond(HttpStatusCode.NotFound) else call.respond(ended)
        }
    }
}

