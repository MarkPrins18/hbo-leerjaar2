package routes

import io.ktor.server.response.*
import io.ktor.server.routing.*
import repositories.CarRepository

fun Route.carRoutes(repository: CarRepository) {
    route("/cars") {
        get {
            val cars = repository.getAllCars()
            call.respond(cars)
        }
    }
}