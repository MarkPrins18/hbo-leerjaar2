import shared.requests.CarChangeDto
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import shared.models.BEVCar
import shared.models.Car
import models.Location
import shared.models.VehicleType
import repositories.CarRepository
import repositories.LocationRepository
import services.LocationService
import services.ResourceNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
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

// ==================== Fakes ====================

private class FakeMapCarRepository(vararg cars: Car) : CarRepository {
    private val cars = cars.associateBy { it.id }

    override suspend fun getCarById(carId: Int): Car? = cars[carId]
    override suspend fun getCarsByUserId(userId: Int): List<Car> = TODO()

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakeMapLocationRepository(vararg locations: Location) : LocationRepository {
    private val locations = locations.associateBy { it.carId }

    override suspend fun getLatestLocationByCarId(carId: Int): Location? = locations[carId]

    override suspend fun addLocation(carId: Int, latitude: Double, longitude: Double, timestamp: Instant): Location = TODO()
}

// ==================== 2. Service: LocationService ====================

class LocationServiceTest {
    private fun service(locationRepository: LocationRepository = FakeMapLocationRepository(testLocation())) =
        LocationService(FakeMapCarRepository(testCar(), testCar(OTHER_CAR_ID)), locationRepository)

    // ---------- getMapsUrl ----------

    @Test
    fun `maps-url wijst naar de laatste locatie van de auto`() = runBlocking {
        assertEquals(MAPS_URL, service().getMapsUrl(CAR_ID))
    }

    @Test
    fun `onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service().getMapsUrl(999) }
        Unit
    }

    @Test
    fun `auto zonder locatie geeft ResourceNotFoundException`() = runBlocking {
        val service = service(locationRepository = FakeMapLocationRepository())

        assertFailsWith<ResourceNotFoundException> { service.getMapsUrl(CAR_ID) }
        Unit
    }
}

// ==================== 3. E2E: map routes ====================

class MapRoutesTest {
    private val seedCarId = 1
    private val seedOwnerId = 1
    private val seedMapsUrl = "https://www.google.com/maps/dir/?api=1&destination=51.64361,4.86044"

    private suspend fun HttpClient.getMaps(carId: Any, token: String? = null): HttpResponse =
        get("/cars/$carId/maps") { token?.let { bearerAuth(it) } }

    // ---------- GET /cars/{carId}/maps ----------

    @Test
    fun `eigenaar krijgt de maps-url van de laatste seedlocatie`() = e2eTest { client ->
        val token = client.login(seedOwnerId)

        val response = client.getMaps(seedCarId, token)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(seedMapsUrl, response.bodyAsText())
    }

    @Test
    fun `gebruiker zonder reservering krijgt de locatie`() = e2eTest { client ->
        val token = client.login(RENTER_ID)

        val response = client.getMaps(seedCarId, token)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(seedMapsUrl, response.bodyAsText())
    }

    @Test
    fun `zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.getMaps(seedCarId).status)
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
