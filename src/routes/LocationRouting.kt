package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.Location
import repositories.LocationRepository

fun Route.locationRoutes(repository: LocationRepository) {
    route("/cars/{carId}/locations") {
        get {
            val carId = call.parameters["carId"]?.toIntOrNull()
            if (carId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig carId")
                return@get
            }
            call.respond(repository.getLocationsForCar(carId))
        }
        get("/latest") {
            val carId = call.parameters["carId"]?.toIntOrNull()
            if (carId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig carId")
                return@get
            }
            val latest = repository.getLatestLocationForCar(carId)
            if (latest == null) call.respond(HttpStatusCode.NotFound) else call.respond(latest)
        }
        post {
            val carId = call.parameters["carId"]?.toIntOrNull()
            if (carId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig carId")
                return@post
            }
            val location = call.receive<Location>().copy(carId = carId)
            call.respond(HttpStatusCode.Created, repository.recordLocation(location))
        }
    }
}

