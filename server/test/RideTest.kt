import dto.RideFinishRequest
import io.ktor.client.HttpClient
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
import models.Ride
import models.RideStatus
import repositories.ReservationRepository
import repositories.RideRepository
import services.ConflictException
import services.ForbiddenException
import services.ResourceNotFoundException
import services.RideService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// ==================== Test data ====================

private const val RENTER_ID = 1
private const val OTHER_USER_ID = 2
private const val RESERVATION_ID = 7

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

    override suspend fun carExists(carId: Int): Boolean = TODO()
    override suspend fun userExists(userId: Int): Boolean = TODO()
    override suspend fun hasActiveOverlap(carId: Int, startTime: Instant, endTime: Instant): Boolean = TODO()
    override suspend fun createReservation(
        carId: Int, userId: Int, startTime: Instant, endTime: Instant, status: ReservationStatus
    ): Reservation = TODO()
    override suspend fun updateStatus(id: Int, status: ReservationStatus): Reservation? = TODO()
}

private class FakeRideRepository : RideRepository {
    private val rides = mutableListOf<Ride>()

    override suspend fun hasRideInProgress(reservationId: Int): Boolean =
        rides.any { it.reservationId == reservationId && it.status == RideStatus.IN_PROGRESS }

    override suspend fun startRide(reservationId: Int, startTime: Instant): Ride =
        Ride(rides.size + 1, reservationId, startTime, null, RideStatus.IN_PROGRESS, null, null, null, null)
            .also { rides += it }

    override suspend fun getRideById(rideId: Int): Ride? = rides.find { it.id == rideId }

    override suspend fun finishRide(
        id: Int, endTime: Instant, distanceM: Double, avgAcceleration: Double, avgDeceleration: Double, bonusPoints: Int
    ): Ride? {
        val index = rides.indexOfFirst { it.id == id }
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
    private val rideRepository = FakeRideRepository()
    private val service = RideService(rideRepository, FakeRideReservationRepository(activeReservation()))

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
}
