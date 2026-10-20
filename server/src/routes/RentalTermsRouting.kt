package routes

import authenticatedUserId
import requireAuthentication
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.util.getOrFail
import dto.RentalTermsRequest
import services.RentalTermsService

fun Route.rentalTermsRoutes(rentalTermsService: RentalTermsService) {
    route("/cars/{id}/rental-terms") {
        get {
            val carId = call.parameters.getOrFail<Int>("id")
            val rentalTerms = rentalTermsService.getRentalTerms(carId)
            call.respond(HttpStatusCode.OK, rentalTerms)
        }
        requireAuthentication {
            put {
                val carId = call.parameters.getOrFail<Int>("id")
                val userId = call.authenticatedUserId()
                val request = call.receive<RentalTermsRequest>()
                val rentalTerms = rentalTermsService.setRentalTerms(carId, userId, request)
                call.respond(HttpStatusCode.OK, rentalTerms)
            }
        }
    }
}
