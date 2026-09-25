import routes.carRoutes
import routes.reservationRoutes
import routes.tripRoutes
import routes.bonusRoutes
import routes.locationRoutes
import routes.photoRoutes
import routes.routeRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository
import repositories.ExposedReservationRepository
import repositories.ExposedTripRepository
import repositories.ExposedBonusRepository
import repositories.ExposedLocationRepository
import repositories.ExposedPhotoRepository
import repositories.OsrmRouteRepository

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        get("/") {
            call.respondText("Hello, World!")
        }
        carRoutes(ExposedCarRepository()) //let op! expliciete parameter.
        reservationRoutes(ExposedReservationRepository())
        tripRoutes(ExposedTripRepository())
        bonusRoutes(ExposedBonusRepository())
        locationRoutes(ExposedLocationRepository())
        photoRoutes(ExposedPhotoRepository())
        routeRoutes(OsrmRouteRepository())
    }
}
