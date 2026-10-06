package routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.util.getOrFail
import repositories.LocationRepository

fun Route.mapsRoutes(
    locationRepository: LocationRepository,
) {

    get("/cars/{carId}/maps") {
        val carId = call.parameters.getOrFail<Int>("carId")

        val location = locationRepository.getLatestLocationByCarId(carId)
            ?: throw NotFoundException("Geen locatie bekend voor deze auto")

        //automatic redirect to maps
       // call.respondRedirect("https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}")

        //Below post the url in Swagger.
        val mapsUrl =
            "https://www.google.com/maps/dir/?api=1&destination=${location.latitude},${location.longitude}"

        call.respond(mapsUrl)
    }
}