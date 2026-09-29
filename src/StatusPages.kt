import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import services.ApiException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.respondText(text = "400: Ongeldige request", status = HttpStatusCode.BadRequest)
        }
        exception<ApiException> { call, cause ->
            call.respondText(text = "${cause.status.value}: ${cause.message}", status = cause.status)
        }
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
}
