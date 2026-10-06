import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import services.ApiException

/**
 * Translates exceptions into HTTP responses. Which exception to throw for which status:
 * - 400 BadRequestException (Ktor): invalid input. Also thrown by receive() and getOrFail().
 * - 401 UnauthorizedException
 * - 403 ForbiddenException
 * - 404 NotFoundException (Ktor)
 * - 409 ConflictException. Duplicate keys and foreign key errors from the database also become 409.
 * - 413 / 415: thrown by Ktor itself (body too large / wrong Content-Type).
 * - 422 UnprocessableException
 * - 502 ExternalServiceException
 * - 500: everything else. The details are logged, not sent to the client.
 * Any other status: throw ApiException(HttpStatusCode.X, "message").
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        // Thrown by getOrFail(); Ktor's own messages are English
        exception<MissingRequestParameterException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, "Parameter '${cause.parameterName}' ontbreekt")
        }
        exception<ParameterConversionException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, "Parameter '${cause.parameterName}' heeft een ongeldige waarde")
        }
        exception<BadRequestException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, cause.message)
        }
        exception<NotFoundException> { call, cause ->
            call.respondError(HttpStatusCode.NotFound, cause.message)
        }
        exception<PayloadTooLargeException> { call, cause ->
            call.respondError(HttpStatusCode.PayloadTooLarge, cause.message)
        }
        exception<UnsupportedMediaTypeException> { call, cause ->
            call.respondError(HttpStatusCode.UnsupportedMediaType, cause.message)
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
