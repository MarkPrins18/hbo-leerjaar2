package routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import repositories.CarRepository
import services.CarService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.util.getOrFail
import requests.*

fun Route.carRoutes(
    repository: CarRepository,
    carService: CarService
) {
    route("/cars") {
        get {
            val cars = repository.getAllCars()
            call.respond(cars)
        }
        get("/{licensePlate}") {
            val licensePlate = call.parameters.getOrFail("licensePlate")

            val car = repository.getCarByLicensePlate(licensePlate)

            if (car == null) {
                call.respond(HttpStatusCode.NotFound, "Car $licensePlate not found")
            } else {
                call.respond(car)
            }
        }
        delete("/{id}") {
            val carId = call.parameters.getOrFail<Int>("id")

            val deleted = repository.deleteCar(carId)

            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Car $carId not found")
            }
        }
        patch("/{id}"){ //is put needed?
            val carId = call.parameters.getOrFail<Int>("id")
            val change = call.receive<CarChangeRequest>()

            val car = carService.changeCar(carId, change)

            if (car == null) {
                call.respond(HttpStatusCode.NotFound, "Car $carId not found")
            } else {
                call.respond(HttpStatusCode.OK, car)
            }
        }
        post("/import") {
            val request = call.receive<CarRequest>()

            val car = carService.importCar(request)

            call.respond(HttpStatusCode.Created, car)
        }
    }
}