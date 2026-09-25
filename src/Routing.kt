import config.configureRdw
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import requests.CarRequest
import services.CarImportService
import routes.carRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository

fun Application.configureRouting() {
    val rdwClient = configureRdw()
    val carRepository = ExposedCarRepository()
    val carImportService = CarImportService(
        rdwClient,
        carRepository
    ) //check why this?
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        carRoutes(
            ExposedCarRepository(),
            carImportService
        ) //let op! expliciete parameter.
    }
}