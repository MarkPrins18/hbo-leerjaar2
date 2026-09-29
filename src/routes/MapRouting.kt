package routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import repositories.LocationRepository

fun Route.mapsRoutes(
    locationRepository: LocationRepository,
) {

    get("/cars/{carId}/maps") {
        val carId = call.parameters["carId"]?.toIntOrNull()
            ?: return@get call.respond(HttpStatusCode.BadRequest, "Ongeldig auto-id")

        val location = locationRepository.getLatestLocationByCarId(carId)
            ?: return@get call.respond(HttpStatusCode.NotFound, "Geen locatie bekend voor deze auto")

        //automatic redirect to maps
       // call.respondRedirect("https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}")

        //Below post the url in Swagger.
        val mapsUrl =
            "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"

        call.respond(mapsUrl)
    }
}