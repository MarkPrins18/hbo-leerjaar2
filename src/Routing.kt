import config.configureRdw
import services.CarService
import routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository
import repositories.ExposedLocationRepository
import repositories.ExposedReservationRepository
import services.ReservationService

fun Application.configureRouting() {
    val rdwClient = configureRdw()
    val carRepository = ExposedCarRepository()
    val locationRepository = ExposedLocationRepository()
    val carImportService = CarService(rdwClient, carRepository)
    val reservationRepository = ExposedReservationRepository()
    val reservationService = ReservationService(reservationRepository)

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        carRoutes(
            ExposedCarRepository(),
            carImportService
        )
        mapsRoutes(locationRepository)
        reservationRoutes(reservationService)
    }
}