import routes.carRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        get("/") {
            call.respondText("Hello, World!")
        }
        carRoutes(ExposedCarRepository()) //let op! expliciete parameter.
    }
}