import clients.GeminiClient
import clients.RdwClient
import config.PhotoStorage
import dto.CarChangeDto
import dto.CarDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import models.*
import repositories.*
import services.CarService
import services.ForbiddenException
import services.ResourceNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

// ==================== Test data ====================

private const val OWNER_ID = 1
private const val OTHER_USER_ID = 2
private const val CAR_ID = 10

// Seed: car 1 (KS711F) is a petrol car of user 1, car 2 (R003BJ) a BEV of user 2
private const val SEED_CAR_COUNT = 8
private const val ICE_CAR_ID = 1
private const val ICE_CAR_PLATE = "KS711F"
private const val BEV_CAR_ID = 2
private const val BEV_CAR_PLATE = "R003BJ"

// Amphia Breda: the seed locations are measured from here
private const val BREDA_LATITUDE = 51.5853
private const val BREDA_LONGITUDE = 4.7946

private fun testCar(roadTaxPerQuarter: Double? = 100.0): BEVCar = BEVCar(
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
    roadTaxPerQuarter = roadTaxPerQuarter,
    batteryCapacityKWh = 60.0
)

// ==================== Fakes ====================

private class FakeOwnedCarRepository(vararg cars: BEVCar) : CarRepository {
    val cars = cars.associateBy { it.id }.toMutableMap()

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? {
        val car = cars[carId] ?: return null
        return car.copy(color = change.color ?: car.color).also { cars[carId] = it }
    }

    override suspend fun deleteCar(carId: Int): Boolean = cars.remove(carId) != null

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
}

private class FakeOwnedCarPhotoRepository : PhotoRepository {
    override suspend fun getFileNamesByCarId(carId: Int): List<String> = emptyList()

    override suspend fun createPhoto(carId: Int, fileName: String): Photo = TODO()
    override suspend fun getPhotosByCarId(carId: Int): List<Photo> = TODO()
    override suspend fun getFileName(photoId: Int): String? = TODO()
}

private class UnusedOwnedCarLocationRepository : LocationRepository {
    override suspend fun getLatestLocationByCarId(carId: Int): Location? = TODO()
}

private class UnusedOwnedCarUserRepository : UserRepository {
    override suspend fun findByEmail(email: String): User? = TODO()
    override suspend fun findAll(): List<User> = TODO()
    override suspend fun userExists(userId: Int): Boolean = TODO()
    override suspend fun create(email: String, passwordHash: String): User = TODO()
}

private class UnusedOwnedCarRentalTermsRepository : RentalTermsRepository {
    override suspend fun carExists(carId: Int): Boolean = TODO()
    override suspend fun getRentalTermsByCarId(carId: Int): RentalTerms? = TODO()
    override suspend fun saveRentalTerms(carId: Int, request: dto.RentalTermsRequest): RentalTerms = TODO()
}

// ==================== 2. Service: CarService ownership ====================

// GeminiClient and RdwClient call the real APIs; these tests never reach them.
class CarOwnershipServiceTest {
    private val carRepository = FakeOwnedCarRepository(testCar())

    private val service = CarService(
        RdwClient(""),
        GeminiClient("", ""),
        carRepository,
        UnusedOwnedCarLocationRepository(),
        UnusedOwnedCarUserRepository(),
        FakeOwnedCarPhotoRepository(),
        PhotoStorage(Path(SystemTemporaryDirectory, "car-test-${Uuid.random()}")),
        UnusedOwnedCarRentalTermsRepository()
    )

    // ---------- updateCar ----------

    @Test
    fun `eigenaar kan zijn auto wijzigen`() = runBlocking {
        val updated = service.updateCar(CAR_ID, OWNER_ID, CarChangeDto(color = "Rood"))

        assertEquals("Rood", updated.color)
        assertEquals("Rood", carRepository.cars[CAR_ID]?.color)
    }

    @Test
    fun `wijzigen door niet-eigenaar geeft ForbiddenException en wijzigt niets`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.updateCar(CAR_ID, OTHER_USER_ID, CarChangeDto(color = "Rood")) }

        assertEquals("Wit", carRepository.cars[CAR_ID]?.color)
    }

    @Test
    fun `wijzigen van onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.updateCar(999, OWNER_ID, CarChangeDto(color = "Rood")) }
        Unit
    }

    // ---------- deleteCar ----------

    @Test
    fun `eigenaar kan zijn auto verwijderen`() = runBlocking {
        service.deleteCar(CAR_ID, OWNER_ID)

        assertNull(carRepository.cars[CAR_ID])
    }

    @Test
    fun `verwijderen door niet-eigenaar geeft ForbiddenException en verwijdert niets`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.deleteCar(CAR_ID, OTHER_USER_ID) }

        assertNotNull(carRepository.cars[CAR_ID])
        Unit
    }

    @Test
    fun `verwijderen van onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.deleteCar(999, OWNER_ID) }
        Unit
    }

    // ---------- fillMissingCostData ----------

    @Test
    fun `eigenaar krijgt een complete auto ongewijzigd terug`() = runBlocking {
        val car = service.fillMissingCostData(CAR_ID, OWNER_ID)

        assertEquals(testCar(), car)
    }

    @Test
    fun `kostengegevens aanvullen door niet-eigenaar geeft ForbiddenException`() = runBlocking {
        val incompleteCarService = CarService(
            RdwClient(""),
            GeminiClient("", ""),
            FakeOwnedCarRepository(testCar(roadTaxPerQuarter = null)),
            UnusedOwnedCarLocationRepository(),
            UnusedOwnedCarUserRepository(),
            FakeOwnedCarPhotoRepository(),
            PhotoStorage(Path(SystemTemporaryDirectory, "car-test-${Uuid.random()}")),
            UnusedOwnedCarRentalTermsRepository()
        )

        assertFailsWith<ForbiddenException> { incompleteCarService.fillMissingCostData(CAR_ID, OTHER_USER_ID) }
        Unit
    }

    @Test
    fun `kostengegevens voor onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.fillMissingCostData(999, OWNER_ID) }
        Unit
    }
}

// ==================== 3. E2E: car routes ====================

class CarRoutesTest {
    private suspend fun HttpClient.patchCar(carId: Int, change: CarChangeDto, token: String? = null): HttpResponse =
        patch("/cars/$carId") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(change)
        }

    private suspend fun HttpClient.importCar(request: CarDto, token: String? = null): HttpResponse =
        post("/cars/import") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    // ---------- GET /cars ----------

    @Test
    fun `zonder filter komen alle seedauto's terug`() = e2eTest { client ->
        val response = client.get("/cars")

        assertEquals(HttpStatusCode.OK, response.status)
        val cars = response.body<List<Car>>()
        assertEquals((1..SEED_CAR_COUNT).toList(), cars.map { it.id }.sorted())
        assertIs<ICECar>(cars.single { it.id == ICE_CAR_ID })
        assertIs<BEVCar>(cars.single { it.id == BEV_CAR_ID })
    }

    @Test
    fun `filter op maxPrice geeft alleen auto's met een prijs tot en met het maximum`() = e2eTest { client ->
        val response = client.get("/cars?maxPrice=45")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(4, 6), response.body<List<Car>>().map { it.id }.sorted())
    }

    @Test
    fun `filter op afstand geeft alleen auto's binnen de straal`() = e2eTest { client ->
        val response = client.get("/cars?maxDistanceKm=10&latitude=$BREDA_LATITUDE&longitude=$BREDA_LONGITUDE")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(1, 4), response.body<List<Car>>().map { it.id }.sorted())
    }

    @Test
    fun `filters op prijs en afstand worden gecombineerd`() = e2eTest { client ->
        val response = client.get("/cars?maxPrice=50&maxDistanceKm=10&latitude=$BREDA_LATITUDE&longitude=$BREDA_LONGITUDE")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(4), response.body<List<Car>>().map { it.id })
    }

    @Test
    fun `maxPrice die geen getal is geeft 400`() = e2eTest { client ->
        assertEquals(HttpStatusCode.BadRequest, client.get("/cars?maxPrice=abc").status)
    }

    @Test
    fun `negatieve maxPrice geeft 400`() = e2eTest { client ->
        assertEquals(HttpStatusCode.BadRequest, client.get("/cars?maxPrice=-1").status)
    }

    @Test
    fun `afstand zonder coordinaten geeft 400`() = e2eTest { client ->
        assertEquals(HttpStatusCode.BadRequest, client.get("/cars?maxDistanceKm=10").status)
    }

    // ---------- GET /cars/{licensePlate} ----------

    @Test
    fun `auto ophalen op kenteken`() = e2eTest { client ->
        val response = client.get("/cars/$ICE_CAR_PLATE")

        assertEquals(HttpStatusCode.OK, response.status)
        val car = assertIs<ICECar>(response.body<Car>())
        assertEquals(ICE_CAR_ID, car.id)
        assertEquals("BMW", car.brand)
    }

    @Test
    fun `kenteken met streepjes en kleine letters wordt genormaliseerd`() = e2eTest { client ->
        val response = client.get("/cars/ks-711-f")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ICE_CAR_ID, response.body<Car>().id)
    }

    @Test
    fun `onbekend kenteken geeft 404`() = e2eTest { client ->
        assertEquals(HttpStatusCode.NotFound, client.get("/cars/XX999X").status)
    }

    // ---------- PATCH /cars/{id} ----------

    @Test
    fun `wijziging wordt teruggegeven en opgeslagen`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        val response = client.patchCar(ICE_CAR_ID, CarChangeDto(color = "ROOD", seats = 2), token)

        assertEquals(HttpStatusCode.OK, response.status)
        val updated = response.body<Car>()
        assertEquals("ROOD", updated.color)
        assertEquals(2, updated.seats)
        val stored = client.get("/cars/$ICE_CAR_PLATE").body<Car>()
        assertEquals("ROOD", stored.color)
        assertEquals(2, stored.seats)
        assertEquals("BMW", stored.brand)
    }

    @Test
    fun `wijzigen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.patchCar(ICE_CAR_ID, CarChangeDto(color = "ROOD")).status)
    }

    @Test
    fun `wijzigen door niet-eigenaar geeft 403 en wijzigt niets`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        val response = client.patchCar(ICE_CAR_ID, CarChangeDto(color = "ROOD"), token)

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("WIT", client.get("/cars/$ICE_CAR_PLATE").body<Car>().color)
    }

    @Test
    fun `eigenaar meesturen bij wijzigen geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        val response = client.patch("/cars/$ICE_CAR_ID") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"userId": $OTHER_USER_ID}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(OWNER_ID, client.get("/cars/$ICE_CAR_PLATE").body<Car>().userId)
    }

    @Test
    fun `auto mag zijn eigen kenteken houden`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.OK, client.patchCar(ICE_CAR_ID, CarChangeDto(licensePlate = ICE_CAR_PLATE), token).status)
    }

    @Test
    fun `kenteken van een andere auto geeft 409`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.Conflict, client.patchCar(ICE_CAR_ID, CarChangeDto(licensePlate = BEV_CAR_PLATE), token).status)
    }

    @Test
    fun `batterijcapaciteit op een brandstofauto geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.patchCar(ICE_CAR_ID, CarChangeDto(batteryCapacityKWh = 50.0), token).status)
    }

    @Test
    fun `waarde buiten het bereik geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.patchCar(ICE_CAR_ID, CarChangeDto(seats = 10), token).status)
    }

    @Test
    fun `wijzigen van onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.NotFound, client.patchCar(999, CarChangeDto(color = "ROOD"), token).status)
    }

    @Test
    fun `ongeldige json bij wijzigen geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        val response = client.patch("/cars/$ICE_CAR_ID") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("{ kapot")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // ---------- DELETE /cars/{id} ----------

    @Test
    fun `verwijderen haalt de auto en zijn gekoppelde gegevens weg`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        val response = client.delete("/cars/$ICE_CAR_ID") { bearerAuth(token) }

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals(HttpStatusCode.NotFound, client.get("/cars/$ICE_CAR_PLATE").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/cars/$ICE_CAR_ID/photos").status)
        assertEquals(HttpStatusCode.NotFound, client.get("/cars/$ICE_CAR_ID/rental-terms").status)
        assertEquals(SEED_CAR_COUNT - 1, client.get("/cars").body<List<Car>>().size)
    }

    @Test
    fun `verwijderen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.delete("/cars/$ICE_CAR_ID").status)
    }

    @Test
    fun `verwijderen door niet-eigenaar geeft 403 en verwijdert niets`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        val response = client.delete("/cars/$ICE_CAR_ID") { bearerAuth(token) }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals(HttpStatusCode.OK, client.get("/cars/$ICE_CAR_PLATE").status)
    }

    @Test
    fun `verwijderen van onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.NotFound, client.delete("/cars/999") { bearerAuth(token) }.status)
    }

    @Test
    fun `ongeldig auto-id bij verwijderen geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.delete("/cars/abc") { bearerAuth(token) }.status)
    }

    // ---------- POST /cars/{id}/cost-data ----------

    @Test
    fun `auto met alle kostengegevens wordt ongewijzigd teruggegeven`() = e2eTest { client ->
        val token = client.login(OWNER_ID)
        client.patchCar(ICE_CAR_ID, CarChangeDto(roadTaxPerQuarter = 150.0), token)

        val response = client.post("/cars/$ICE_CAR_ID/cost-data") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        val car = response.body<Car>()
        assertEquals(150.0, car.roadTaxPerQuarter)
        assertEquals(8.1, car.consumptionCombined)
    }

    @Test
    fun `kostengegevens zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.post("/cars/$ICE_CAR_ID/cost-data").status)
    }

    @Test
    fun `kostengegevens door niet-eigenaar geeft 403`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        assertEquals(HttpStatusCode.Forbidden, client.post("/cars/$ICE_CAR_ID/cost-data") { bearerAuth(token) }.status)
    }

    @Test
    fun `kostengegevens voor onbekende auto geeft 404`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.NotFound, client.post("/cars/999/cost-data") { bearerAuth(token) }.status)
    }

    // ---------- POST /cars/import ----------
    // Only the paths that fail before the RDW is called; a successful import needs the live RDW API.

    @Test
    fun `importeren zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.importCar(CarDto(licensePlate = "AB123C")).status)
    }

    @Test
    fun `bestaand kenteken importeren geeft 409`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.Conflict, client.importCar(CarDto(licensePlate = "ks-711-f"), token).status)
    }

    @Test
    fun `leeg kenteken importeren geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.importCar(CarDto(licensePlate = " - "), token).status)
    }

    @Test
    fun `negatieve tankinhoud importeren geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        assertEquals(HttpStatusCode.BadRequest, client.importCar(CarDto(licensePlate = "AB123C", tankCapacityL = -1.0), token).status)
    }

    @Test
    fun `eigenaar meesturen bij importeren geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        val response = client.post("/cars/import") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("""{"licensePlate": "AB123C", "userId": $OTHER_USER_ID}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `import zonder kenteken geeft 400`() = e2eTest { client ->
        val token = client.login(OWNER_ID)

        val response = client.post("/cars/import") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody("{}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
