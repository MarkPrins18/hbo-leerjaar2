import config.configureRdw
import config.jwtConfig
import services.CarService
import routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository
import repositories.ExposedLocationRepository
import repositories.ExposedReservationRepository
import repositories.ExposedUserRepository
import services.AuthService
import services.ReservationService
import services.TokenService
import services.UserService

fun Application.configureRouting() {
    val rdwClient = configureRdw()
    val carRepository = ExposedCarRepository()
    val locationRepository = ExposedLocationRepository()
    val carService = CarService(
        rdwClient,
        carRepository,
        locationRepository
    )
    val reservationRepository = ExposedReservationRepository()
    val reservationService = ReservationService(reservationRepository)
    val userRepository = ExposedUserRepository()
    val userService = UserService(userRepository)
    val authService = AuthService(userRepository, TokenService(jwtConfig()))
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        carRoutes(carService)
        mapsRoutes(locationRepository)
        reservationRoutes(reservationService)
        userRoutes(userService, authService)
    }
}