package routes

import authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import dto.RideFinishRequest
import requireAuthentication
import services.RideService

fun Route.rideRoutes(rideService: RideService) {
    requireAuthentication {
        post("/reservations/{id}/rides") {
            val reservationId = call.parameters.getOrFail<Int>("id")
            val userId = call.authenticatedUserId()
            val ride = rideService.startRide(reservationId, userId)
            call.respond(HttpStatusCode.Created, ride)
        }
        patch("/rides/{id}/finish") {
            val rideId = call.parameters.getOrFail<Int>("id")
            val userId = call.authenticatedUserId()
            val request = call.receive<RideFinishRequest>()
            val ride = rideService.finishRide(rideId, userId, request)
            call.respond(HttpStatusCode.OK, ride)

        }
    }
}