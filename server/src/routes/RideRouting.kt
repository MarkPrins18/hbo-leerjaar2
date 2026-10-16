package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import dto.RideFinishRequest
import services.RideService

fun Route.rideRoutes(rideService: RideService) {
    post("/reservations/{id}/rides") {
        val reservationId = call.parameters.getOrFail<Int>("id")
        val ride = rideService.startRide(reservationId)
        call.respond(HttpStatusCode.Created, ride)
    }
    patch("/rides/{id}/finish") {
        val rideId = call.parameters.getOrFail<Int>("id")
        val request = call.receive<RideFinishRequest>()
        val ride = rideService.finishRide(rideId, request)
        call.respond(HttpStatusCode.OK, ride)
    }
}