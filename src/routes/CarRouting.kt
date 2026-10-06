package routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.CarService
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.util.getOrFail
import requests.*

fun Route.carRoutes(
    carService: CarService
) {
    route("/cars") {
        get {
            val filter = call.request.queryParameters.toCarFilter()
            call.respond(carService.getCars(filter))
        }
        get("/{licensePlate}") {
            val licensePlate = call.parameters.getOrFail("licensePlate")

            call.respond(carService.getCarByLicensePlate(licensePlate))
        }
        delete("/{id}") {
            val carId = call.parameters.getOrFail<Int>("id")

            carService.deleteCar(carId)

            call.respond(HttpStatusCode.NoContent)
        }
        patch("/{id}"){ //is put needed?
            val carId = call.parameters.getOrFail<Int>("id")
            val change = call.receive<CarChangeRequest>()

            call.respond(carService.updateCar(carId, change))
        }
        post("/import") {
            val request = call.receive<CarRequest>()

            val car = carService.importCar(request)

            call.respond(HttpStatusCode.Created, car)
        }
    }
}

private fun Parameters.optionalDouble(name: String): Double? {
    val raw = this[name] ?: return null
    return raw.toDoubleOrNull()
        ?: throw BadRequestException("'$name' moet een getal zijn")
}

private fun Parameters.toCarFilter(): CarFilter = CarFilter(
    maxPrice = optionalDouble("maxPrice"),
    maxDistanceKm = optionalDouble("maxDistanceKm"),
    latitude = optionalDouble("latitude"),
    longitude = optionalDouble("longitude"),
)