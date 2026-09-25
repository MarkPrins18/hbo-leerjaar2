package routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import repositories.CarRepository
import services.CarImportService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import requests.CarRequest

fun Route.carRoutes(
    repository: CarRepository,
    carImportService: CarImportService
) {
    route("/cars") {
        get {
            val cars = repository.getAllCars()
            call.respond(cars)
        }
        post("/import") {
            val request = call.receive<CarRequest>()

            val car = carImportService.importCar(request)

            call.respond(HttpStatusCode.Created, car)
        }
    }
}