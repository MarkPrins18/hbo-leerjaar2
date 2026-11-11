package routes

import authenticatedUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import requireAuthentication
import services.LocationService
import shared.requests.LocationRequest

fun Route.mapRoutes(
    locationService: LocationService,
) {
    requireAuthentication {
        get("/cars/{carId}/maps") {
            val carId = call.parameters.getOrFail<Int>("carId")
            val userId = call.authenticatedUserId()

            val mapsUrl = locationService.getMapsUrl(carId, userId)

            //automatic redirect to maps
            // call.respondRedirect(mapsUrl)

            //Below post the url in Swagger.
            call.respond(mapsUrl)
        }
        put("/cars/{id}/location") {
            val carId = call.parameters.getOrFail<Int>("id")
            val userId = call.authenticatedUserId()
            val request = call.receive<LocationRequest>()

            call.respond(HttpStatusCode.OK, locationService.setLocation(carId, userId, request))
        }
    }
}
