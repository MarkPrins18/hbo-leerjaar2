import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.CannotTransformContentToTypeException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import services.ApiException

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Thrown by getOrFail(); Ktor's own messages are English
        exception<MissingRequestParameterException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, "Parameter '${cause.parameterName}' ontbreekt")
        }
        exception<ParameterConversionException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, "Parameter '${cause.parameterName}' heeft een ongeldige waarde")
        }
        // Thrown by receive() when the body is not valid JSON or a field is missing or has the wrong type
        exception<BadRequestException> { call, _ ->
            call.respondError(
                HttpStatusCode.BadRequest,
                "Ongeldige body: controleer of het geldige JSON is met alle verplichte velden en geldige waarden"
            )
        }
        exception<NotFoundException> { call, _ ->
            call.respondError(HttpStatusCode.NotFound, "Niet gevonden")
        }
        exception<PayloadTooLargeException> { call, _ ->
            call.respondError(HttpStatusCode.PayloadTooLarge, "De body is te groot")
        }
        exception<UnsupportedMediaTypeException> { call, _ ->
            call.respondError(HttpStatusCode.UnsupportedMediaType, "Content-Type wordt niet ondersteund: gebruik application/json, of multipart/form-data voor foto's")
        }
        // Thrown by receive() when the body has a Content-Type without a JSON converter, e.g. text/plain
        exception<CannotTransformContentToTypeException> { call, _ ->
            call.respondError(HttpStatusCode.UnsupportedMediaType, "Content-Type wordt niet ondersteund: gebruik application/json, of multipart/form-data voor foto's")
        }
        exception<ApiException> { call, cause ->
            if (cause.status.value >= 500) {
                call.application.log.error("${cause.status.value}: ${cause.message}", cause)
            }
            call.respondError(cause.status, cause.message)
        }
        exception<ExposedSQLException> { call, cause ->
            // SQLState class 23 = integrity constraint violation (duplicate key or foreign key)
            if (cause.sqlState.startsWith("23")) {
                call.respondError(
                    HttpStatusCode.Conflict,
                    "De actie botst met bestaande data, bijvoorbeeld een dubbel kenteken of gekoppelde gegevens"
                )
            } else {
                call.application.log.error("Database error", cause)
                call.respondError(HttpStatusCode.InternalServerError, "Databasefout")
            }
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respondError(HttpStatusCode.InternalServerError, "Er ging iets mis op de server")
        }
    }
}

private suspend fun ApplicationCall.respondError(status: HttpStatusCode, message: String?) {
    respondText(text = "${status.value}: ${message ?: status.description}", status = status)
}
