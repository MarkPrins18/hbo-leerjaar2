import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import models.Location
import repositories.CarRepository
import repositories.LocationRepository
import repositories.ReservationRepository
import services.ForbiddenException
import services.LocationService
import services.ResourceNotFoundException
import services.ValidationException
import services.validate
import shared.models.BEVCar
import shared.models.Car
import shared.models.Reservation
import shared.models.ReservationStatus
import shared.models.VehicleType
import shared.requests.CarChangeDto
import shared.requests.LocationRequest
import shared.responses.LocationResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant

// ==================== Test data ====================

private const val OWNER_ID = 1
private const val OTHER_USER_ID = 2
private const val CAR_ID = 10

private val newLocation = LocationRequest(latitude = 51.5853, longitude = 4.7946)

private fun testCar(): Car = BEVCar(
    id = CAR_ID,
    userId = OWNER_ID,
    licensePlate = "AB-123-C",
    brand = "Tesla",
    model = "Model 3",
    productionYear = 2022,
    trim = null,
    color = "Wit",
    seats = 5,
    doors = 4,
    vehicleType = VehicleType.PASSENGER_CAR,
    readyToDriveWeightKg = 1800,
    consumptionCombined = 15.0,
    co2EmissionCombined = 0.0,
    roadTaxPerQuarter = null,
    batteryCapacityKWh = 60.0
)

// ==================== Fakes ====================

private class FakeLocationCarRepository(vararg cars: Car) : CarRepository {
    private val cars = cars.associateBy { it.id }

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun getCarsByUserId(userId: Int): List<Car> = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakeLocationRepository : LocationRepository {
    val locations = mutableListOf<Location>()

    override suspend fun addLocation(carId: Int, latitude: Double, longitude: Double, timestamp: Instant): Location =
        Location(locations.size + 1, carId, latitude = latitude, longitude = longitude, speed = 0.0, timestamp = timestamp)
            .also { locations += it }

    override suspend fun getLatestLocationByCarId(carId: Int): Location? = TODO()
}

private class UnusedLocationReservationRepository : ReservationRepository {
    override suspend fun createReservationIfAvailable(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus, totalPrice: Double
    ): Reservation? = TODO()
    override suspend fun getReservationById(id: Int): Reservation? = TODO()
    override suspend fun getReservationsByCarAndUser(carId: Int, userId: Int): List<Reservation> = TODO()
    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? = TODO()
    override suspend fun getReservationsByUserId(userId: Int): List<Reservation> = TODO()
    override suspend fun getReservedCarIds(startTime: Instant, endTime: Instant): Set<Int> = TODO()
}

// ==================== 1. Validation: LocationRequest ====================

class LocationRequestValidationTest {
    @Test
    fun `locatie op de grenzen is geldig`() {
        LocationRequest(90.0, 180.0).validate()
        LocationRequest(-90.0, -180.0).validate()
    }

    @Test
    fun `latitude boven 90 geeft ValidationException`() {
        assertFailsWith<ValidationException> { LocationRequest(90.1, 0.0).validate() }
    }

    @Test
    fun `longitude onder -180 geeft ValidationException`() {
        assertFailsWith<ValidationException> { LocationRequest(0.0, -180.1).validate() }
    }

    @Test
    fun `NaN geeft ValidationException`() {
        assertFailsWith<ValidationException> { LocationRequest(Double.NaN, Double.NaN).validate() }
    }
}

// ==================== 2. Service: LocationService.setLocation ====================

class SetLocationServiceTest {
    private val locationRepository = FakeLocationRepository()
    private val service = LocationService(
        FakeLocationCarRepository(testCar()), locationRepository, UnusedLocationReservationRepository()
    )

    // ---------- setLocation ----------

    @Test
    fun `eigenaar kan de locatie van zijn auto instellen`() = runBlocking {
        val response = service.setLocation(CAR_ID, OWNER_ID, newLocation)

        assertEquals(CAR_ID, response.carId)
        assertEquals(newLocation.latitude, response.latitude)
        assertEquals(newLocation.longitude, response.longitude)
        assertEquals(1, locationRepository.locations.size)
    }

    @Test
    fun `locatie instellen door een niet-eigenaar geeft ForbiddenException zonder nieuwe rij`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.setLocation(CAR_ID, OTHER_USER_ID, newLocation) }
        assertTrue(locationRepository.locations.isEmpty())
    }

    @Test
    fun `locatie instellen voor een onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.setLocation(999, OWNER_ID, newLocation) }
        Unit
    }

    @Test
    fun `ongeldige latitude geeft ValidationException zonder nieuwe rij`() = runBlocking {
        assertFailsWith<ValidationException> { service.setLocation(CAR_ID, OWNER_ID, newLocation.copy(latitude = -91.0)) }
        assertTrue(locationRepository.locations.isEmpty())
    }
}

// ==================== 3. E2E: car location routes ====================

class CarLocationRoutesTest {
    // Seed: auto 1 is van gebruiker 1
    private val seedCarId = 1
    private val seedOwnerId = 1

    private suspend fun HttpClient.putLocation(
        carId: Int, token: String? = null, request: LocationRequest = newLocation
    ): HttpResponse =
        put("/cars/$carId/location") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    // ---------- PUT /cars/{id}/location ----------

    @Test
    fun `eigenaar stelt de locatie in en die wordt de huidige locatie`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        val response = client.putLocation(seedCarId, token)

        assertEquals(HttpStatusCode.OK, response.status)
        val location = response.body<LocationResponse>()
        assertEquals(seedCarId, location.carId)
        assertEquals(newLocation.latitude, location.latitude)
        assertEquals(newLocation.longitude, location.longitude)
        val mapsUrl = client.get("/cars/$seedCarId/maps") { bearerAuth(token) }.bodyAsText()
        assertEquals("https://www.google.com/maps/dir/?api=1&destination=51.5853,4.7946", mapsUrl)
    }

    @Test
    fun `locatie instellen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.putLocation(seedCarId).status)
    }

    @Test
    fun `locatie instellen voor de auto van een ander geeft 403`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        assertEquals(HttpStatusCode.Forbidden, client.putLocation(seedCarId, token).status)
    }

    @Test
    fun `locatie instellen voor een onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        assertEquals(HttpStatusCode.NotFound, client.putLocation(999, token).status)
    }

    @Test
    fun `ongeldige longitude geeft 400`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        val response = client.putLocation(seedCarId, token, newLocation.copy(longitude = 200.0))

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
