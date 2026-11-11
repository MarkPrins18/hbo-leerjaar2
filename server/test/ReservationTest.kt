import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import models.User
import repositories.CarRepository
import repositories.RentalTermsRepository
import shared.models.RentalTerms
import shared.requests.RentalTermsRequest
import shared.requests.CarChangeDto
import shared.models.BEVCar
import shared.models.Car
import shared.models.Reservation
import shared.models.ReservationStatus
import shared.models.VehicleType
import repositories.ReservationRepository
import repositories.UserRepository
import shared.requests.ReservationRequest
import services.ConflictException
import services.ForbiddenException
import services.ReservationService
import services.ResourceNotFoundException
import services.UnprocessableException
import services.ValidationException
import services.calculateTotalPrice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

// ==================== Test data ====================

private const val RENTER_ID = 1
private const val OTHER_USER_ID = 2
private const val OWNER_ID = 3
private const val CAR_ID = 10
private const val CAR_WITHOUT_TERMS_ID = 11
private const val PRICE_PER_DAY = 89.0

private val start = Instant.parse("2099-03-01T09:00:00Z")
private val end = Instant.parse("2099-03-01T17:00:00Z")

private fun request(carId: Int = CAR_ID, startTime: Instant = start, endTime: Instant = end) =
    ReservationRequest(carId, startTime, endTime)

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

// ==================== Fakes ====================

private class FakeReservationCarRepository(vararg cars: Car) : CarRepository {
    private val cars = cars.associateBy { it.id }

    override suspend fun getCarById(carId: Int): Car? = cars[carId]

    override suspend fun getAllCars(): List<Car> = TODO()
    override suspend fun getCarByLicensePlate(licensePlate: String): Car? = TODO()
    override suspend fun getCarsByUserId(userId: Int): List<Car> = TODO()
    override suspend fun createCar(car: Car): Car = TODO()
    override suspend fun updateCar(carId: Int, change: CarChangeDto): Car? = TODO()
    override suspend fun deleteCar(carId: Int): Boolean = TODO()
}

private class FakeReservationUserRepository(private vararg val userIds: Int) : UserRepository {
    override suspend fun userExists(userId: Int): Boolean = userId in userIds

    override suspend fun findByEmail(email: String): User? = TODO()
    override suspend fun findById(userId: Int): User? = TODO()
    override suspend fun findAll(): List<User> = TODO()
    override suspend fun create(email: String, passwordHash: String): User = TODO()
}

private class FakeReservationRentalTermsRepository(vararg rentalTerms: RentalTerms) : RentalTermsRepository {
    private val rentalTerms = rentalTerms.associateBy { it.carId }

    override suspend fun getRentalTermsByCarId(carId: Int): RentalTerms? = rentalTerms[carId]

    override suspend fun carExists(carId: Int): Boolean = TODO()
    override suspend fun saveRentalTerms(carId: Int, request: RentalTermsRequest): RentalTerms = TODO()
}

private class FakeReservationRepository : ReservationRepository {
    private val reservations = mutableListOf<Reservation>()

    override suspend fun createReservationIfAvailable(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus, totalPrice: Double
    ): Reservation? {
        val hasOverlap = reservations.any {
            it.carId == carId &&
                it.status in listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED) &&
                it.startTime < endTime && it.endTime > startTime
        }
        if (hasOverlap) return null
        return Reservation(reservations.size + 1, carId, userId, startTime, endTime, status, totalPrice)
            .also { reservations += it }
    }

    fun count(): Int = reservations.size

    override suspend fun getReservationById(id: Int): Reservation? = reservations.find { it.id == id }

    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? {
        val index = reservations.indexOfFirst { it.id == id }
        if (index == -1) return null
        reservations[index] = reservations[index].copy(status = status)
        return reservations[index]
    }

    override suspend fun getReservationsByUserId(userId: Int): List<Reservation> =
        reservations.filter { it.userId == userId }

    override suspend fun getReservationsByCarAndUser(carId: Int, userId: Int): List<Reservation> = TODO()
}

// ==================== 1. Calculation: calculateTotalPrice ====================

class TotalPriceTest {
    @Test
    fun `precies 24 uur is een dag`() {
        assertEquals(89.0, calculateTotalPrice(PRICE_PER_DAY, start, start + 24.hours))
    }

    @Test
    fun `24 uur en 1 minuut zijn twee begonnen dagen`() {
        assertEquals(178.0, calculateTotalPrice(PRICE_PER_DAY, start, start + 24.hours + 1.minutes))
    }

    @Test
    fun `1 uur kost een hele dag`() {
        assertEquals(89.0, calculateTotalPrice(PRICE_PER_DAY, start, start + 1.hours))
    }

    @Test
    fun `prijs wordt afgerond op centen`() {
        assertEquals(99.99, calculateTotalPrice(33.33, start, start + 72.hours))
    }
}

// ==================== 2. Service: ReservationService ====================

class ReservationServiceTest {
    private val repository = FakeReservationRepository()
    private val service = ReservationService(
        repository,
        FakeReservationCarRepository(testCar(), testCar(CAR_WITHOUT_TERMS_ID)),
        FakeReservationUserRepository(RENTER_ID, OTHER_USER_ID, OWNER_ID),
        FakeReservationRentalTermsRepository(
            RentalTerms(id = 1, carId = CAR_ID, pricePerDay = PRICE_PER_DAY, pickupLocation = "Breda")
        )
    )

    // ---------- createReservation ----------

    @Test
    fun `reservering komt op naam van de ingelogde gebruiker`() = runBlocking {
        val reservation = service.createReservation(RENTER_ID, request())

        assertEquals(RENTER_ID, reservation.userId)
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
    }

    @Test
    fun `reservering krijgt de prijs per begonnen dag`() = runBlocking {
        val reservation = service.createReservation(RENTER_ID, request(endTime = start + 25.hours))

        assertEquals(178.0, reservation.totalPrice)
    }

    @Test
    fun `auto zonder huurvoorwaarden geeft UnprocessableException en reserveert niets`() = runBlocking {
        assertFailsWith<UnprocessableException> { service.createReservation(RENTER_ID, request(carId = CAR_WITHOUT_TERMS_ID)) }
        assertEquals(0, repository.count())
    }

    @Test
    fun `starttijd in het verleden geeft ValidationException`() = runBlocking {
        assertFailsWith<ValidationException> {
            service.createReservation(RENTER_ID, request(startTime = Instant.parse("2020-01-01T09:00:00Z")))
        }
        Unit
    }

    @Test
    fun `onbekende auto geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.createReservation(RENTER_ID, request(carId = 999)) }
        Unit
    }

    @Test
    fun `onbekende gebruiker geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.createReservation(999, request()) }
        Unit
    }

    @Test
    fun `overlappend tijdsblok geeft ConflictException`() = runBlocking {
        service.createReservation(RENTER_ID, request())

        assertFailsWith<ConflictException> { service.createReservation(OTHER_USER_ID, request()) }
        assertEquals(1, repository.count())
    }

    @Test
    fun `eigen auto reserveren geeft ForbiddenException`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.createReservation(OWNER_ID, request()) }
        assertEquals(0, repository.count())
    }

    // ---------- cancelReservation ----------

    @Test
    fun `huurder kan zijn eigen reservering annuleren`() = runBlocking {
        val reservation = service.createReservation(RENTER_ID, request())

        val cancelled = service.cancelReservation(reservation.id, RENTER_ID)

        assertEquals(ReservationStatus.CANCELLED, cancelled.status)
    }

    @Test
    fun `annuleren door een andere gebruiker geeft ForbiddenException`() = runBlocking {
        val reservation = service.createReservation(RENTER_ID, request())

        assertFailsWith<ForbiddenException> { service.cancelReservation(reservation.id, OTHER_USER_ID) }
        assertEquals(ReservationStatus.CONFIRMED, repository.getReservationById(reservation.id)?.status)
    }

    @Test
    fun `onbekende reservering annuleren geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.cancelReservation(999, RENTER_ID) }
        Unit
    }

    @Test
    fun `al geannuleerde reservering annuleren geeft ConflictException`() = runBlocking {
        val reservation = service.createReservation(RENTER_ID, request())
        service.cancelReservation(reservation.id, RENTER_ID)

        assertFailsWith<ConflictException> { service.cancelReservation(reservation.id, RENTER_ID) }
        Unit
    }

    // ---------- getReservationsForUser ----------

    @Test
    fun `gebruiker krijgt alleen zijn eigen reserveringen, gesorteerd op starttijd`() = runBlocking {
        val later = service.createReservation(RENTER_ID, request())
        val earlier = service.createReservation(
            RENTER_ID, request(startTime = Instant.parse("2098-03-01T09:00:00Z"), endTime = Instant.parse("2098-03-01T17:00:00Z"))
        )
        service.createReservation(
            OTHER_USER_ID, request(startTime = Instant.parse("2097-03-01T09:00:00Z"), endTime = Instant.parse("2097-03-01T17:00:00Z"))
        )

        val reservations = service.getReservationsForUser(RENTER_ID)

        assertEquals(listOf(earlier, later), reservations)
    }

    @Test
    fun `gebruiker zonder reserveringen krijgt een lege lijst`() = runBlocking {
        assertEquals(emptyList<Reservation>(), service.getReservationsForUser(OTHER_USER_ID))
    }
}

// ==================== 3. E2E: reservation routes ====================

class ReservationRoutesTest {
    // Seed: reservering 4 is van gebruiker 3, start op 2027-01-15 en is CONFIRMED.
    private val seedReservationId = 4
    private val seedRenterId = 3
    private val otherUserId = 2
    private val thirdUserId = 3
    private val seedOwnerOfCar1 = 1
    // Seed: auto 8 is van gebruiker 1 en heeft geen huurvoorwaarden; auto 1 kost 89.00 per dag
    private val carWithoutTermsId = 8
    private val seedPricePerDayCar1 = 89.0

    private suspend fun HttpClient.reserve(body: String, token: String? = null): HttpResponse =
        post("/reservations") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend fun HttpClient.cancel(id: Int, token: String? = null): HttpResponse =
        patch("/reservations/$id/cancel") { token?.let { bearerAuth(it) } }

    private val validBody = """{ "carId": 1, "startTime": "2099-03-01T09:00:00Z", "endTime": "2099-03-01T17:00:00Z" }"""

    // ---------- POST /reservations ----------

    @Test
    fun `reserveren met token geeft 201 op naam van de ingelogde gebruiker`() = e2eTest { client ->
        val token = client.login(otherUserId)

        val response = client.reserve(validBody, token)

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(otherUserId, response.body<Reservation>().userId)
    }

    @Test
    fun `reservering van 8 uur kost een dag huur`() = e2eTest { client ->
        val token = client.login(otherUserId)

        val response = client.reserve(validBody, token)

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(seedPricePerDayCar1, response.body<Reservation>().totalPrice)
    }

    @Test
    fun `auto zonder huurvoorwaarden reserveren geeft 422`() = e2eTest { client ->
        val token = client.login(otherUserId)
        val body = """{ "carId": $carWithoutTermsId, "startTime": "2099-03-01T09:00:00Z", "endTime": "2099-03-01T17:00:00Z" }"""

        assertEquals(HttpStatusCode.UnprocessableEntity, client.reserve(body, token).status)
    }

    @Test
    fun `reserveren zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.reserve(validBody).status)
    }

    @Test
    fun `userId meesturen in de body geeft 400`() = e2eTest { client ->
        val token = client.login(otherUserId)
        val body = """{ "carId": 1, "userId": 3, "startTime": "2099-03-01T09:00:00Z", "endTime": "2099-03-01T17:00:00Z" }"""

        assertEquals(HttpStatusCode.BadRequest, client.reserve(body, token).status)
    }

    @Test
    fun `eigen auto reserveren geeft 403`() = e2eTest { client ->
        val token = client.login(seedOwnerOfCar1)

        assertEquals(HttpStatusCode.Forbidden, client.reserve(validBody, token).status)
    }

    @Test
    fun `overlappende reservering geeft 409`() = e2eTest { client ->
        client.reserve(validBody, client.login(otherUserId))
        val thirdUserToken = client.login(thirdUserId)

        assertEquals(HttpStatusCode.Conflict, client.reserve(validBody, thirdUserToken).status)
    }

    // ---------- GET /reservations ----------

    @Test
    fun `eigen reserveringen opvragen geeft 200 gesorteerd op starttijd`() = e2eTest { client ->
        val token = client.login(seedRenterId)

        val response = client.get("/reservations") { bearerAuth(token) }

        // Seed: gebruiker 3 heeft reservering 1 (2026-09-20) en 4 (2027-01-15)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(1, seedReservationId), response.body<List<Reservation>>().map { it.id })
    }

    @Test
    fun `reserveringen van een ander zijn niet zichtbaar`() = e2eTest { client ->
        val otherToken = client.login(otherUserId)
        val otherReservation = client.reserve(validBody, otherToken).body<Reservation>()
        val token = client.login(seedRenterId)

        val reservations = client.get("/reservations") { bearerAuth(token) }.body<List<Reservation>>()

        assertTrue(reservations.all { it.userId == seedRenterId })
        assertTrue(reservations.none { it.id == otherReservation.id })
    }

    @Test
    fun `reserveringen opvragen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/reservations").status)
    }

    // ---------- PATCH /reservations/{id}/cancel ----------

    @Test
    fun `huurder annuleert zijn reservering en krijgt 200`() = e2eTest { client ->
        val token = client.login(seedRenterId)

        val response = client.cancel(seedReservationId, token)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ReservationStatus.CANCELLED, response.body<Reservation>().status)
    }

    @Test
    fun `annuleren zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.cancel(seedReservationId).status)
    }

    @Test
    fun `reservering van een ander annuleren geeft 403`() = e2eTest { client ->
        val token = client.login(otherUserId)

        assertEquals(HttpStatusCode.Forbidden, client.cancel(seedReservationId, token).status)
    }
}
