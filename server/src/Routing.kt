import config.configureGemini
import config.configureRdw
import config.jwtConfig
import config.photoStorage
import services.CarCostService
import services.CarImportService
import services.CarService
import routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.swagger.*
import repositories.ExposedCarRepository
import repositories.ExposedLocationRepository
import repositories.ExposedPhotoRepository
import repositories.ExposedRentalTermsRepository
import repositories.ExposedReservationRepository
import repositories.ExposedUserRepository
import services.AuthService
import services.MapService
import services.PhotoService
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
    val userRepository = ExposedUserRepository()
    val photoRepository = ExposedPhotoRepository()
    val photoStorage = photoStorage()
    val rentalTermsRepository = ExposedRentalTermsRepository()
    val carService = CarService(carRepository, locationRepository, photoRepository, photoStorage, rentalTermsRepository)
    val carImportService = CarImportService(rdwClient, carRepository, userRepository)
    val carCostService = CarCostService(configureGemini(), carRepository)
    val photoService = PhotoService(photoRepository, carRepository, photoStorage)
    val rentalTermsService = RentalTermsService(rentalTermsRepository, carRepository)
    val reservationRepository = ExposedReservationRepository()
    val reservationService = ReservationService(reservationRepository)
    val rideRepository = ExposedRideRepository()
    val rideService = RideService(rideRepository, reservationRepository)
    val mapService = MapService(carRepository, locationRepository, reservationRepository)
    val userService = UserService(userRepository)
    val authService = AuthService(userRepository, TokenService(jwtConfig()))
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        carRoutes(carService, carImportService, carCostService)
        photoRoutes(photoService)
        mapsRoutes(mapService)
        rentalTermsRoutes(rentalTermsService)
        reservationRoutes(reservationService)
        rideRoutes(rideService)
        userRoutes(userService, authService)
    }
}