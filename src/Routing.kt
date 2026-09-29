import config.configureRdw
import services.CarService
import routes.carRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository

fun Application.configureRouting() {
    val rdwClient = configureRdw()
    val carRepository = ExposedCarRepository()
    val carImportService = CarService(
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