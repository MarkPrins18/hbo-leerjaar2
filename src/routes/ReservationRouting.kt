package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import models.Reservation
import models.ReservationStatus
import repositories.ReservationRepository

// Issue #9 - Reservering-en-huurflow

@Serializable
data class ReservationStatusUpdate(val status: ReservationStatus)

fun Route.reservationRoutes(repository: ReservationRepository) {
    route("/reservations") {
        get {
            call.respond(repository.getAllReservations())
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig id")
                return@get
            }
            val reservation = repository.getReservationById(id)
            if (reservation == null) call.respond(HttpStatusCode.NotFound) else call.respond(reservation)
        }
        post {
            val reservation = call.receive<Reservation>()
            val created = repository.createReservation(reservation)
            if (created == null) {
                call.respond(HttpStatusCode.Conflict, "Auto is in deze periode al gereserveerd")
            } else {
                call.respond(HttpStatusCode.Created, created)
            }
        }
        patch("/{id}/status") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig id")
                return@patch
            }
            val update = call.receive<ReservationStatusUpdate>()
            val updated = repository.updateReservationStatus(id, update.status)
            if (updated == null) call.respond(HttpStatusCode.NotFound) else call.respond(updated)
        }
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig id")
                return@delete
            }
            val deleted = repository.deleteReservation(id)
            call.respond(if (deleted) HttpStatusCode.NoContent else HttpStatusCode.NotFound)
        }
    }
}

