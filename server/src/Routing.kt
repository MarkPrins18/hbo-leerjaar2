import config.configureRdw
import config.jwtConfig
import services.CarService
import routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository
import repositories.ExposedLocationRepository
import repositories.ExposedRentalTermsRepository
import repositories.ExposedReservationRepository
import repositories.ExposedUserRepository
import services.AuthService
import services.RentalTermsService
import services.ReservationService
import repositories.ExposedRideRepository
import services.RideService
import services.TokenService
import services.UserService

fun Application.configureRouting() {
    val rdwClient = configureRdw()
    val carRepository = ExposedCarRepository()
    val locationRepository = ExposedLocationRepository()
    val rentalTermsRepository = ExposedRentalTermsRepository()
    val carService = CarService(
        rdwClient,
        carRepository,
        locationRepository,
        rentalTermsRepository
    )
    val rentalTermsService = RentalTermsService(rentalTermsRepository)
    val reservationRepository = ExposedReservationRepository()
    val reservationService = ReservationService(reservationRepository)
    val rideRepository = ExposedRideRepository()
    val rideService = RideService(rideRepository, reservationRepository)
    val userRepository = ExposedUserRepository()
    val userService = UserService(userRepository)
    val tokenService = TokenService(jwtConfig())
    val authService = AuthService(userRepository, tokenService)
    configureAuthentication(tokenService)
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        carRoutes(carService)
        mapsRoutes(locationRepository)
        rentalTermsRoutes(rentalTermsService)
        reservationRoutes(reservationService)
        rideRoutes(rideService)
        userRoutes(userService, authService)
    }
}