package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import repositories.BonusRepository

// Issue #12 - Bonuspuntensysteem

@Serializable
data class BonusPointsTotal(val renterId: Int, val totalPoints: Int)

fun Route.bonusRoutes(repository: BonusRepository) {
    route("/renters/{renterId}/bonus-points") {
        get {
            val renterId = call.parameters["renterId"]?.toIntOrNull()
            if (renterId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig renterId")
                return@get
            }
            call.respond(BonusPointsTotal(renterId, repository.getTotalPointsForRenter(renterId)))
        }
        get("/history") {
            val renterId = call.parameters["renterId"]?.toIntOrNull()
            if (renterId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig renterId")
                return@get
            }
            call.respond(repository.getAwardsForRenter(renterId))
        }
    }
}

