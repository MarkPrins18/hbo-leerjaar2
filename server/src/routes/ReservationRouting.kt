package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import requests.ReservationRequest
import services.ReservationService

fun Route.reservationRoutes(reservationService: ReservationService) {
    route("/reservations") {
        post {
            val request = call.receive<ReservationRequest>()
            val reservation = reservationService.createReservation(request)
            call.respond(HttpStatusCode.Created, reservation)
        }
        patch("/{id}/cancel") {
            val id = call.parameters.getOrFail<Int>("id")
            val reservation = reservationService.cancelReservation(id)
            call.respond(HttpStatusCode.OK, reservation)
        }
    }
}