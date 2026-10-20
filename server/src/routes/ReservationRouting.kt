package routes

import authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import requests.ReservationRequest
import requireAuthentication
import services.ReservationService

fun Route.reservationRoutes(reservationService: ReservationService) {
    requireAuthentication {
        route("/reservations") {
            post {
                val userId = call.authenticatedUserId()
                val request = call.receive<ReservationRequest>()
                val reservation = reservationService.createReservation(userId, request)
                call.respond(HttpStatusCode.Created, reservation)
            }
            patch("/{id}/cancel") {
                val id = call.parameters.getOrFail<Int>("id")
                val userId = call.authenticatedUserId()
                val reservation = reservationService.cancelReservation(id, userId)
                call.respond(HttpStatusCode.OK, reservation)
            }
        }
    }
}