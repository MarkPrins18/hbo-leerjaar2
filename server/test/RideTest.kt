import shared.dto.RideFinishRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import shared.models.Reservation
import shared.models.ReservationStatus
import shared.models.Ride
import shared.models.RideStatus
import repositories.ExposedRideRepository
import repositories.ReservationRepository
import repositories.RideRepository
import repositories.ExposedReservationRepository
import services.ConflictException
import services.ForbiddenException
import services.ResourceNotFoundException
import services.RideService
import services.ValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// ==================== Test data ====================

private const val RENTER_ID = 1
private const val OTHER_USER_ID = 2
private const val RESERVATION_ID = 7
private const val MAX_DISTANCE_M = 6_000_000.0

private val calmRide = RideFinishRequest(distanceM = 12_500.0, avgAcceleration = 1.0, avgDeceleration = 1.0)

/** A confirmed reservation that is active right now, so a ride can start. */
private fun activeReservation(): Reservation {
    val now = Clock.System.now()
    return Reservation(RESERVATION_ID, 10, RENTER_ID, now - 1.hours, now + 1.hours, ReservationStatus.CONFIRMED)
}

// ==================== Fakes ====================

private class FakeRideReservationRepository(vararg reservations: Reservation) : ReservationRepository {
    private val reservations = reservations.associateBy { it.id }

    override suspend fun getReservationById(id: Int): Reservation? = reservations[id]

    override suspend fun createReservationIfAvailable(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus
    ): Reservation? = TODO()
    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? = TODO()
    override suspend fun getReservationsByCarAndUser(carId: Int, userId: Int): List<Reservation> = TODO()
    override suspend fun getReservationsByUserId(userId: Int): List<Reservation> = TODO()
}

/** [staleReads]: getRideById returns the ride as it was at start, as if another request finishes it in between. */
private class FakeRideRepository(vararg reservations: Reservation, private val staleReads: Boolean = false) : RideRepository {
    private val rides = mutableListOf<Ride>()
    private val reservations = reservations.associateBy { it.id }

    override suspend fun getRideInProgressByUserId(userId: Int): Ride? =
        rides.find { it.status == RideStatus.IN_PROGRESS && reservations[it.reservationId]?.userId == userId }

    fun hasRideInProgress(reservationId: Int): Boolean =
        rides.any { it.reservationId == reservationId && it.status == RideStatus.IN_PROGRESS }

    override suspend fun startRideIfNoneInProgress(reservationId: Int, startTime: Instant): Ride? {
        if (hasRideInProgress(reservationId)) return null
        return Ride(rides.size + 1, reservationId, startTime, null, RideStatus.IN_PROGRESS, null, null, null, null)
            .also { rides += it }
    }

    override suspend fun getRideById(rideId: Int): Ride? {
        val ride = rides.find { it.id == rideId } ?: return null
        return if (staleReads) ride.copy(status = RideStatus.IN_PROGRESS) else ride
    }

    override suspend fun finishRide(
        id: Int, endTime: Instant, distanceM: Double, avgAcceleration: Double, avgDeceleration: Double, bonusPoints: Int
    ): Ride? {
        val index = rides.indexOfFirst { it.id == id && it.status == RideStatus.IN_PROGRESS }
        if (index == -1) return null
        rides[index] = rides[index].copy(
            endTime = endTime, status = RideStatus.COMPLETED, distanceM = distanceM,
            avgAcceleration = avgAcceleration, avgDeceleration = avgDeceleration, bonusPoints = bonusPoints
        )
        return rides[index]
    }
}

// ==================== 2. Service: RideService ====================

class RideServiceTest {
    private val reservation = activeReservation()
    private val rideRepository = FakeRideRepository(reservation)
    private val service = RideService(rideRepository, FakeRideReservationRepository(reservation))

    // ---------- startRide ----------

    @Test
    fun `huurder kan een rit starten`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)

        assertEquals(RideStatus.IN_PROGRESS, ride.status)
    }

    @Test
    fun `rit starten door een andere gebruiker geeft ForbiddenException`() = runBlocking {
        assertFailsWith<ForbiddenException> { service.startRide(RESERVATION_ID, OTHER_USER_ID) }
        assertEquals(false, rideRepository.hasRideInProgress(RESERVATION_ID))
    }

    @Test
    fun `rit starten voor onbekende reservering geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.startRide(999, RENTER_ID) }
        Unit
    }

    // ---------- finishRide ----------

    @Test
    fun `huurder kan zijn rit afronden en krijgt bonuspunten`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)

        val finished = service.finishRide(ride.id, RENTER_ID, calmRide)

        assertEquals(RideStatus.COMPLETED, finished.status)
        assertEquals(12, finished.bonusPoints)
    }

    @Test
    fun `rit afronden door een andere gebruiker geeft ForbiddenException`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)

        assertFailsWith<ForbiddenException> { service.finishRide(ride.id, OTHER_USER_ID, calmRide) }
        assertEquals(RideStatus.IN_PROGRESS, rideRepository.getRideById(ride.id)?.status)
    }

    @Test
    fun `onbekende rit afronden geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.finishRide(999, RENTER_ID, calmRide) }
        Unit
    }

    @Test
    fun `afgeronde rit nogmaals afronden geeft ConflictException`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)
        service.finishRide(ride.id, RENTER_ID, calmRide)

        assertFailsWith<ConflictException> { service.finishRide(ride.id, RENTER_ID, calmRide) }
        Unit
    }

    @Test
    fun `rit die tussendoor door een ander verzoek is afgerond geeft ConflictException`() = runBlocking {
        val staleRepository = FakeRideRepository(activeReservation(), staleReads = true)
        val staleService = RideService(staleRepository, FakeRideReservationRepository(activeReservation()))
        val ride = staleService.startRide(RESERVATION_ID, RENTER_ID)
        staleService.finishRide(ride.id, RENTER_ID, calmRide)

        assertFailsWith<ConflictException> {
            staleService.finishRide(ride.id, RENTER_ID, calmRide.copy(distanceM = 99_000.0))
        }
        assertEquals(12, staleRepository.getRideById(ride.id)?.bonusPoints)
    }

    @Test
    fun `tweede rit starten terwijl er al een loopt geeft ConflictException`() = runBlocking {
        service.startRide(RESERVATION_ID, RENTER_ID)

        assertFailsWith<ConflictException> { service.startRide(RESERVATION_ID, RENTER_ID) }
        Unit
    }

    // ---------- getActiveRide ----------

    @Test
    fun `huurder krijgt zijn lopende rit`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)

        assertEquals(ride, service.getActiveRide(RENTER_ID))
    }

    @Test
    fun `lopende rit van een ander is niet zichtbaar en geeft null`() = runBlocking {
        service.startRide(RESERVATION_ID, RENTER_ID)

        assertNull(service.getActiveRide(OTHER_USER_ID))
    }

    @Test
    fun `afstand van precies 6000 km is toegestaan`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)

        val finished = service.finishRide(ride.id, RENTER_ID, calmRide.copy(distanceM = MAX_DISTANCE_M))

        assertEquals(RideStatus.COMPLETED, finished.status)
    }

    @Test
    fun `afstand boven 6000 km geeft ValidationException`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)

        assertFailsWith<ValidationException> {
            service.finishRide(ride.id, RENTER_ID, calmRide.copy(distanceM = MAX_DISTANCE_M + 1))
        }
        assertEquals(RideStatus.IN_PROGRESS, rideRepository.getRideById(ride.id)?.status)
    }

    @Test
    fun `afgeronde rit telt niet als lopende rit`() = runBlocking {
        val ride = service.startRide(RESERVATION_ID, RENTER_ID)
        service.finishRide(ride.id, RENTER_ID, calmRide)

        assertNull(service.getActiveRide(RENTER_ID))
    }
}

// ==================== 3. E2E: ride routes ====================

class RideRoutesTest {
    // Seed: reservering 4 is van gebruiker 1 en start pas op 2027-01-15.
    // Seed: rit 1 hoort bij reservering 1 van gebruiker 1 en is al COMPLETED.
    private val seedReservationId = 4
    private val seedRideId = 1
    private val seedRenterId = 1
    private val otherUserId = 2

    private suspend fun HttpClient.startRide(reservationId: Int, token: String? = null): HttpResponse =
        post("/reservations/$reservationId/rides") { token?.let { bearerAuth(it) } }

    private suspend fun HttpClient.finishRide(rideId: Int, token: String? = null): HttpResponse =
        patch("/rides/$rideId/finish") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(calmRide)
        }

    // ---------- POST /reservations/{id}/rides ----------

    @Test
    fun `rit starten zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.startRide(seedReservationId).status)
    }

    @Test
    fun `rit starten op reservering van een ander geeft 403`() = e2eTest { client ->
        val token = client.login(otherUserId)

        assertEquals(HttpStatusCode.Forbidden, client.startRide(seedReservationId, token).status)
    }

    @Test
    fun `huurder komt door de controle en krijgt 409 omdat de reservering nog niet actief is`() = e2eTest { client ->
        val token = client.login(seedRenterId)

        assertEquals(HttpStatusCode.Conflict, client.startRide(seedReservationId, token).status)
    }

    // ---------- PATCH /rides/{id}/finish ----------

    @Test
    fun `rit afronden zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.finishRide(seedRideId).status)
    }

    @Test
    fun `rit van een ander afronden geeft 403`() = e2eTest { client ->
        val token = client.login(otherUserId)

        assertEquals(HttpStatusCode.Forbidden, client.finishRide(seedRideId, token).status)
    }

    @Test
    fun `huurder komt door de controle en krijgt 409 omdat de rit al is afgerond`() = e2eTest { client ->
        val token = client.login(seedRenterId)

        assertEquals(HttpStatusCode.Conflict, client.finishRide(seedRideId, token).status)
    }

    // ---------- start en finish op een reservering die nu actief is ----------

    @Test
    fun `tweede rit starten of tweede keer afronden geeft 409`() = e2eTest { client ->
        // The API only accepts reservations in the future, so the active one is inserted directly.
        val token = client.login(otherUserId)
        val now = Clock.System.now()
        val reservation = ExposedReservationRepository().createReservationIfAvailable(
            1, otherUserId, now - 1.hours, now + 1.hours, ReservationStatus.CONFIRMED
        )!!

        val started = client.startRide(reservation.id, token)
        val secondStart = client.startRide(reservation.id, token)
        val rideId = started.body<Ride>().id
        val finished = client.finishRide(rideId, token)
        val secondFinish = client.finishRide(rideId, token)

        assertEquals(HttpStatusCode.Created, started.status)
        assertEquals(HttpStatusCode.Conflict, secondStart.status)
        assertEquals(HttpStatusCode.OK, finished.status)
        assertEquals(HttpStatusCode.Conflict, secondFinish.status)
    }

    // ---------- GET /rides/active ----------

    @Test
    fun `lopende rit opvragen geeft 200 met de rit van de huurder`() = e2eTest { client ->
        val token = client.login(seedRenterId)
        // A ride can only start during the reservation, so put one in the database directly
        val started = ExposedRideRepository().startRideIfNoneInProgress(seedReservationId, Clock.System.now())!!

        val response = client.get("/rides/active") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(started.id, response.body<Ride>().id)
    }

    @Test
    fun `lopende rit van een ander opvragen geeft 204 zonder body`() = e2eTest { client ->
        val token = client.login(otherUserId)
        ExposedRideRepository().startRideIfNoneInProgress(seedReservationId, Clock.System.now())!!

        val response = client.get("/rides/active") { bearerAuth(token) }

        assertEquals(HttpStatusCode.NoContent, response.status)
        assertEquals("", response.bodyAsText())
    }

    @Test
    fun `lopende rit opvragen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/rides/active").status)
    }
}
