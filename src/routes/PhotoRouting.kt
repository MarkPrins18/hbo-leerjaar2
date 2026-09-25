package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import models.Photo
import repositories.PhotoRepository

fun Route.photoRoutes(repository: PhotoRepository) {
    route("/cars/{carId}/photos") {
        get {
            val carId = call.parameters["carId"]?.toIntOrNull()
            if (carId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig carId")
                return@get
            }
            call.respond(repository.getPhotosForCar(carId))
        }
        post {
            val carId = call.parameters["carId"]?.toIntOrNull()
            if (carId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig carId")
                return@post
            }
            val photo = call.receive<Photo>().copy(carId = carId)
            call.respond(HttpStatusCode.Created, repository.addPhoto(photo))
        }
        delete("/{photoId}") {
            val photoId = call.parameters["photoId"]?.toIntOrNull()
            if (photoId == null) {
                call.respond(HttpStatusCode.BadRequest, "Ongeldig photoId")
                return@delete
            }
            val deleted = repository.deletePhoto(photoId)
            call.respond(if (deleted) HttpStatusCode.NoContent else HttpStatusCode.NotFound)
        }
    }
}
