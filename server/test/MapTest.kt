import dto.CarChangeDto
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import models.BEVCar
import models.Car
import models.Location
import models.Reservation
import models.ReservationStatus
import models.VehicleType
import repositories.CarRepository
import repositories.LocationRepository
import repositories.ReservationRepository
import services.ForbiddenException
import services.LocationService
import services.ResourceNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// ==================== Test data ====================

private const val OWNER_ID = 1
private const val RENTER_ID = 2
private const val CAR_ID = 10
private const val OTHER_CAR_ID = 11
private const val LATITUDE = 51.5853
private const val LONGITUDE = 4.7946
private const val MAPS_URL = "https://www.google.com/maps/dir/?api=1&destination=$LATITUDE,$LONGITUDE"

private fun testCar(id: Int = CAR_ID): Car = BEVCar(
    id = id,
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

private fun testLocation(carId: Int = CAR_ID): Location = Location(
    id = 1,
    carId = carId,
    latitude = LATITUDE,
    longitude = LONGITUDE,
    speed = null,
    timestamp = Clock.System.now()
)

private fun reservation(
    startTime: Instant,
    endTime: Instant,
    status: ReservationStatus = ReservationStatus.CONFIRMED,
    carId: Int = CAR_ID
): Reservation = Reservation(1, carId, RENTER_ID, startTime, endTime, status)

private fun activeReservation(
    status: ReservationStatus = ReservationStatus.CONFIRMED,
    carId: Int = CAR_ID
): Reservation {
    val now = Clock.System.now()
    return reservation(now - 1.hours, now + 1.hours, status, carId)
}

// ==================== Fakes ====================

private class FakeMapCarRepository(vararg cars: Car) : CarRepository {
    private val cars = cars.associateBy { it.id }

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakeMapLocationRepository(vararg locations: Location) : LocationRepository {
    private val locations = locations.associateBy { it.carId }

    override suspend fun getLatestLocationByCarId(carId: Int): Location? = locations[carId]
}

private class FakeMapReservationRepository(vararg reservations: Reservation) : ReservationRepository {
    private val reservations = reservations.toList()

    override suspend fun getReservationsByCarAndUser(carId: Int, userId: Int): List<Reservation> =
        reservations.filter { it.carId == carId && it.userId == userId }

    override suspend fun carExists(carId: Int): Boolean = TODO()
    override suspend fun userExists(userId: Int): Boolean = TODO()
    override suspend fun hasActiveOverlap(carId: Int, startTime: Instant, endTime: Instant): Boolean = TODO()
    override suspend fun createReservation(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus
    ): Reservation = TODO()
    override suspend fun getReservationById(id: Int): Reservation? = TODO()
    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? = TODO()
}

// ==================== 2. Service: LocationService ====================

class LocationServiceTest {
    private fun service(
        vararg reservations: Reservation,
        locationRepository: LocationRepository = FakeMapLocationRepository(testLocation())
    ) = LocationService(
        FakeMapCarRepository(testCar(), testCar(OTHER_CAR_ID)),
        locationRepository,
        FakeMapReservationRepository(*reservations)
    )

    // ---------- getMapsUrl ----------

    @Test
    fun `eigenaar krijgt de maps-url van de laatste locatie`() = runBlocking {
        assertEquals(MAPS_URL, service().getMapsUrl(CAR_ID, OWNER_ID))
    }

    @Test
    fun `huurder met lopende bevestigde reservering krijgt de maps-url`() = runBlocking {
        assertEquals(MAPS_URL, service(activeReservation()).getMapsUrl(CAR_ID, RENTER_ID))
    }

    @Test
    fun `gebruiker zonder reservering geeft ForbiddenException`() = runBlocking {
        assertFailsWith<ForbiddenException> { service().getMapsUrl(CAR_ID, RENTER_ID) }
        Unit
    }

    @Test
    fun `huurder met toekomstige reservering geeft ForbiddenException`() = runBlocking {
        val now = Clock.System.now()
        val service = service(reservation(now + 1.hours, now + 2.hours))

        assertFailsWith<ForbiddenException> { service.getMapsUrl(CAR_ID, RENTER_ID) }
        Unit
    }

    @Test
    fun `huurder met verlopen reservering geeft ForbiddenException`() = runBlocking {
        val now = Clock.System.now()
        val service = service(reservation(now - 2.hours, now - 1.hours))

        assertFailsWith<ForbiddenException> { service.getMapsUrl(CAR_ID, RENTER_ID) }
        Unit
    }

    @Test
    fun `huurder met lopende maar niet bevestigde reservering geeft ForbiddenException`() = runBlocking {
        val service = service(
            activeReservation(ReservationStatus.PENDING),
            activeReservation(ReservationStatus.CANCELLED),
            activeReservation(ReservationStatus.COMPLETED)
        )

        assertFailsWith<ForbiddenException> { service.getMapsUrl(CAR_ID, RENTER_ID) }
        Unit
    }

    @Test
    fun `lopende reservering voor een andere auto geeft ForbiddenException`() = runBlocking {
        val service = service(activeReservation(carId = OTHER_CAR_ID))

        assertFailsWith<ForbiddenException> { service.getMapsUrl(CAR_ID, RENTER_ID) }
        Unit
    }

    @Test
    fun `onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service().getMapsUrl(999, OWNER_ID) }
        Unit
    }

    @Test
    fun `auto zonder locatie geeft ResourceNotFoundException`() = runBlocking {
        val service = service(locationRepository = FakeMapLocationRepository())

        assertFailsWith<ResourceNotFoundException> { service.getMapsUrl(CAR_ID, OWNER_ID) }
        Unit
    }
}

// ==================== 3. E2E: map routes ====================

class MapRoutesTest {
    private val seedCarId = 1
    private val seedOwnerId = 1

    private suspend fun HttpClient.getMaps(carId: Any, token: String? = null): HttpResponse =
        get("/cars/$carId/maps") { token?.let { bearerAuth(it) } }

    // ---------- GET /cars/{carId}/maps ----------

    @Test
    fun `eigenaar krijgt de maps-url van de laatste seedlocatie`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        val response = client.getMaps(seedCarId, token)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("https://www.google.com/maps/dir/?api=1&destination=51.64361,4.86044", response.bodyAsText())
    }

    @Test
    fun `zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.getMaps(seedCarId).status)
    }

    @Test
    fun `niet-eigenaar zonder reservering geeft 403`() = e2eTest { client ->
        val token = client.login(RENTER_ID)

        assertEquals(HttpStatusCode.Forbidden, client.getMaps(seedCarId, token).status)
    }

    @Test
    fun `huurder met toekomstige seedreservering geeft 403`() = e2eTest { client ->
        // seed: user 4 has a pending reservation in 2027 for car 5, which is owned by user 2
        val token = client.login(4)

        assertEquals(HttpStatusCode.Forbidden, client.getMaps(5, token).status)
    }

    @Test
    fun `onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        assertEquals(HttpStatusCode.NotFound, client.getMaps(999, token).status)
    }

    @Test
    fun `ongeldig auto-id geeft 400`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        assertEquals(HttpStatusCode.BadRequest, client.getMaps("abc", token).status)
    }
}
