import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import models.Reservation
import models.ReservationStatus
import repositories.ReservationRepository
import requests.ReservationRequest
import services.ConflictException
import services.ForbiddenException
import services.ReservationService
import services.ResourceNotFoundException
import services.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

// ==================== Test data ====================

private const val RENTER_ID = 1
private const val OTHER_USER_ID = 2
private const val CAR_ID = 10

private val start = Instant.parse("2099-03-01T09:00:00Z")
private val end = Instant.parse("2099-03-01T17:00:00Z")

private fun request(carId: Int = CAR_ID, startTime: Instant = start, endTime: Instant = end) =
    ReservationRequest(carId, startTime, endTime)

// ==================== Fakes ====================

private class FakeReservationRepository(
    private val carIds: Set<Int> = setOf(CAR_ID),
    private val userIds: Set<Int> = setOf(RENTER_ID, OTHER_USER_ID)
) : ReservationRepository {
    private val reservations = mutableListOf<Reservation>()

    override suspend fun carExists(carId: Int): Boolean = carId in carIds
    override suspend fun userExists(userId: Int): Boolean = userId in userIds

    override suspend fun hasActiveOverlap(carId: Int, startTime: Instant, endTime: Instant): Boolean =
        reservations.any {
            it.carId == carId &&
                it.status in listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED) &&
                it.startTime < endTime && it.endTime > startTime
        }

    override suspend fun createReservation(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus
    ): Reservation = Reservation(reservations.size + 1, carId, userId, startTime, endTime, status)
        .also { reservations += it }

    override suspend fun getReservationById(id: Int): Reservation? = reservations.find { it.id == id }

    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? {
        val index = reservations.indexOfFirst { it.id == id }
        if (index == -1) return null
        reservations[index] = reservations[index].copy(status = status)
        return reservations[index]
    }
}

// ==================== 2. Service: ReservationService ====================

class ReservationServiceTest {
    private val repository = FakeReservationRepository()
    private val service = ReservationService(repository)

    // ---------- createReservation ----------

    @Test
    fun `reservering komt op naam van de ingelogde gebruiker`() = runBlocking {
        val reservation = service.createReservation(RENTER_ID, request())

        assertEquals(RENTER_ID, reservation.userId)
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
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
    fun `overlappend tijdsblok geeft ConflictException`() = runBlocking {
        service.createReservation(RENTER_ID, request())

        assertFailsWith<ConflictException> { service.createReservation(OTHER_USER_ID, request()) }
        Unit
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
}

// ==================== 3. E2E: reservation routes ====================

class ReservationRoutesTest {
    // Seed: reservering 4 is van gebruiker 1, start op 2027-01-15 en is CONFIRMED.
    private val seedReservationId = 4
    private val seedRenterId = 1
    private val otherUserId = 2

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
    fun `reserveren zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.reserve(validBody).status)
    }

    @Test
    fun `userId meesturen in de body geeft 400`() = e2eTest { client ->
        val token = client.login(otherUserId)
        val body = """{ "carId": 1, "userId": 3, "startTime": "2099-03-01T09:00:00Z", "endTime": "2099-03-01T17:00:00Z" }"""

        assertEquals(HttpStatusCode.BadRequest, client.reserve(body, token).status)
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
