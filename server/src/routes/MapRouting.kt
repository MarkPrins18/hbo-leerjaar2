package routes

import authenticatedUserId
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import requireAuthentication
import services.MapService

fun Route.mapsRoutes(
    mapService: MapService,
) {
    requireAuthentication {
        get("/cars/{id}/maps") {
            val carId = call.parameters.getOrFail<Int>("id")
            val userId = call.authenticatedUserId()

            val mapsUrl = mapService.getMapsUrl(carId, userId)

            //automatic redirect to maps
            // call.respondRedirect(mapsUrl)

            //Below post the url in Swagger.
            call.respond(mapsUrl)
        }
    }
}
