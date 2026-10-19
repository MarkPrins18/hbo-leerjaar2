package routes

import authenticatedUserId
import requireAuthentication
import io.ktor.server.response.*
import io.ktor.server.routing.*
import services.CarService
import services.DEFAULT_ANNUAL_KM
import services.ValidationException
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.request.receive
import io.ktor.server.util.getOrFail
import dto.*

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
        requireAuthentication {
            delete("/{id}") {
                val carId = call.parameters.getOrFail<Int>("id")
                val userId = call.authenticatedUserId()

                carService.deleteCar(carId, userId)

                call.respond(HttpStatusCode.NoContent)
            }
            patch("/{id}"){ //is put needed?
                val carId = call.parameters.getOrFail<Int>("id")
                val userId = call.authenticatedUserId()
                val change = call.receive<CarChangeDto>()

                call.respond(carService.updateCar(carId, userId, change))
            }
            get("/{id}/fuel-cost") {
                val carId = call.parameters.getOrFail<Int>("id")
                val distanceKm = call.request.queryParameters.getOrFail<Double>("distanceKm")
                val userId = call.authenticatedUserId()

                call.respond(carService.getFuelCost(carId, userId, distanceKm))
            }
            get("/{id}/tco") {
                val carId = call.parameters.getOrFail<Int>("id")
                val annualKm = call.request.queryParameters.optionalDouble("annualKm") ?: DEFAULT_ANNUAL_KM
                val userId = call.authenticatedUserId()

                call.respond(carService.getTco(carId, userId, annualKm))
            }
            post("/{id}/cost-data") { //remove: test route until the total cost calculation exists
                val carId = call.parameters.getOrFail<Int>("id")
                val userId = call.authenticatedUserId()

                call.respond(carService.fillMissingCostData(carId, userId))
            }
            post("/import") {
                val userId = call.authenticatedUserId()
                val request = call.receive<CarDto>()

                val car = carService.importCar(request, userId)

                call.respond(HttpStatusCode.Created, car)
            }
        }
    }
}

private fun Parameters.optionalDouble(name: String): Double? {
    val raw = this[name] ?: return null
    return raw.toDoubleOrNull()
        ?: throw ValidationException("'$name' moet een getal zijn")
}

private fun Parameters.toCarFilter(): CarFilter = CarFilter(
    maxPrice = optionalDouble("maxPrice"),
    maxDistanceKm = optionalDouble("maxDistanceKm"),
    latitude = optionalDouble("latitude"),
    longitude = optionalDouble("longitude"),
)