package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import repositories.RouteRepository

// Issue #10 - Route naar auto / externe routering

@Serializable
data class RouteRequest(
    val startLatitude: Double,
    val startLongitude: Double,
    val endLatitude: Double,
    val endLongitude: Double
)

fun Route.routeRoutes(repository: RouteRepository) {
    route("/route") {
        post {
            val request = call.receive<RouteRequest>()
            val result = repository.calculateRoute(
                request.startLatitude, request.startLongitude,
                request.endLatitude, request.endLongitude
            )
            if (result == null) {
                call.respond(HttpStatusCode.BadGateway, "Routeringsdienst kon geen route berekenen")
            } else {
                call.respond(result)
            }
        }
    }
}

