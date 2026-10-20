import dto.CarChangeDto
import dto.RentalTermsRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import models.*
import repositories.CarRepository
import repositories.RentalTermsRepository
import services.ForbiddenException
import services.RentalTermsService
import services.ResourceNotFoundException
import services.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

// ==================== Test data ====================

private const val OWNER_ID = 1
private const val OTHER_USER_ID = 2
private const val CAR_ID = 10

// Seed: car 1 is owned by user 1 and rents for 89.00 per day
private const val SEED_CAR_ID = 1
private const val SEED_PRICE_PER_DAY = 89.0

private val validRequest = RentalTermsRequest(pricePerDay = 60.0, pickupLocation = "Grote Markt 1, Breda")

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

private class FakeRentalTermsCarRepository(vararg cars: Car) : CarRepository {
    private val cars = cars.associateBy { it.id }

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakeRentalTermsRepository : RentalTermsRepository {
    val saved = mutableMapOf<Int, RentalTerms>()

    override suspend fun saveRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms =
        RentalTerms(id = 1, carId = carId, pricePerDay = request.pricePerDay, pickupLocation = request.pickupLocation)
            .also { saved[carId] = it }

    override suspend fun carExists(carId: Int): Boolean = TODO()
    override suspend fun getRentalTermsByCarId(carId: Int): RentalTerms? = TODO()
}

// ==================== 2. Service: RentalTermsService ====================

class RentalTermsServiceTest {
    private val rentalTermsRepository = FakeRentalTermsRepository()
    private val service = RentalTermsService(rentalTermsRepository, FakeRentalTermsCarRepository(testCar()))

    // ---------- setRentalTerms ----------

    @Test
    fun `eigenaar kan verhuurvoorwaarden instellen`() = runBlocking {
        val result = service.setRentalTerms(CAR_ID, OWNER_ID, validRequest)

        assertEquals(60.0, result.pricePerDay)
        assertEquals(result, rentalTermsRepository.saved[CAR_ID])
    }

    @Test
    fun `niet-eigenaar geeft ForbiddenException en slaat niets op`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.setRentalTerms(CAR_ID, OTHER_USER_ID, validRequest) }

        assertNull(rentalTermsRepository.saved[CAR_ID])
    }

    @Test
    fun `onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.setRentalTerms(999, OWNER_ID, validRequest) }
        Unit
    }

    @Test
    fun `ongeldige prijs geeft ValidationException en slaat niets op`() = runBlocking {
        assertFailsWith<ValidationException> { service.setRentalTerms(CAR_ID, OWNER_ID, validRequest.copy(pricePerDay = 0.0)) }

        assertNull(rentalTermsRepository.saved[CAR_ID])
    }
}

// ==================== 3. E2E: rental terms routes ====================

class RentalTermsRoutesTest {
    private suspend fun HttpClient.putRentalTerms(carId: Int, request: RentalTermsRequest, token: String? = null): HttpResponse =
        put("/cars/$carId/rental-terms") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    // ---------- GET /cars/{id}/rental-terms ----------

    @Test
    fun `verhuurvoorwaarden ophalen kan zonder token`() = e2eTest { client ->
        val response = client.get("/cars/$SEED_CAR_ID/rental-terms")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(SEED_PRICE_PER_DAY, response.body<RentalTerms>().pricePerDay)
    }

    // ---------- PUT /cars/{id}/rental-terms ----------

    @Test
    fun `eigenaar stelt verhuurvoorwaarden in`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        val response = client.putRentalTerms(SEED_CAR_ID, validRequest, token)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(60.0, response.body<RentalTerms>().pricePerDay)
        assertEquals(60.0, client.get("/cars/$SEED_CAR_ID/rental-terms").body<RentalTerms>().pricePerDay)
    }

    @Test
    fun `instellen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.putRentalTerms(SEED_CAR_ID, validRequest).status)
    }

    @Test
    fun `instellen door niet-eigenaar geeft 403 en wijzigt niets`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        val response = client.putRentalTerms(SEED_CAR_ID, validRequest, token)

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(SEED_PRICE_PER_DAY, client.get("/cars/$SEED_CAR_ID/rental-terms").body<RentalTerms>().pricePerDay)
    }

    @Test
    fun `instellen voor onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.NotFound, client.putRentalTerms(999, validRequest, token).status)
    }

    @Test
    fun `ongeldige prijs geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.putRentalTerms(SEED_CAR_ID, validRequest.copy(pricePerDay = 0.0), token).status)
    }
}
