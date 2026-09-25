package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.Car
import repositories.CarRepository

fun Route.carRoutes(repository: CarRepository) {
    route("/cars") {
        get {
            val cars = repository.getAllCars()
            call.respond(cars)
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig id")
                return@get
            }
            val car = repository.getCarById(id)
            if (car == null) call.respond(HttpStatusCode.NotFound) else call.respond(car)
        }
        post {
            val car = call.receive<Car>()
            call.respond(HttpStatusCode.Created, repository.createCar(car))
        }
    }
}