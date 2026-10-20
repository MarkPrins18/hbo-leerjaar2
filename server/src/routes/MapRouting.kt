package routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import requireAuthentication
import services.LocationService

fun Route.mapRoutes(
    locationService: LocationService) {
    requireAuthentication {
        get("/cars/{carId}/maps") {
            val carId = call.parameters.getOrFail<Int>("carId")

            call.respond(locationService.getMapsUrl(carId))
        }
    }
}