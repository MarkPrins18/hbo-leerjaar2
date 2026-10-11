package routes

import authenticatedUserId
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import requireAuthentication
import services.PhotoService
import services.ValidationException

fun Route.photoRoutes(
    photoService: PhotoService
) {
    requireAuthentication {
        post("/cars/{id}/photos") {
            val carId = call.parameters.getOrFail<Int>("id")
            val userId = call.authenticatedUserId()

            var data: ByteArray? = null
            call.receiveMultipart().forEachPart { part ->
                if (part is PartData.FileItem && part.name == "photo" && data == null) {
                    data = part.provider().readRemaining().readByteArray()
                }
                part.release()
            }

            val photo = photoService.uploadPhoto(
                carId,
                userId,
                data ?: throw ValidationException("Geen foto meegestuurd in veld 'photo'")
            )

            call.respond(HttpStatusCode.Created, photo)
        }
    }
    get("/cars/{id}/photos") {
        val carId = call.parameters.getOrFail<Int>("id")

        call.respond(photoService.getPhotos(carId))
    }
    get("/photos/{id}") {
        val photoId = call.parameters.getOrFail<Int>("id")

        call.respondBytes(photoService.getPhotoData(photoId), ContentType.Image.JPEG)
    }
}
