import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import repositories.ExposedReservationRepository
import repositories.ExposedRideRepository
import shared.models.ReservationStatus

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import models.User
import repositories.RideRepository
import repositories.UserRepository
import shared.models.Ride
import shared.requests.RideFinishRequest
import services.ResourceNotFoundException
import services.UserService
import shared.responses.UserResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// ==================== Test data ====================

private const val USER_ID = 1
private const val OTHER_USER_ID = 2

private fun testUser(id: Int = USER_ID) = User(id, "eigenaar$id@carshare.nl", "hash")

// ==================== Fakes ====================

private class FakeUserRepository(vararg users: User) : UserRepository {
    private val users = users.associateBy { it.id }

    override suspend fun findById(userId: Int): User? = users[userId]

    override suspend fun findByEmail(email: String): User? = TODO()
    override suspend fun findAll(): List<User> = TODO()
    override suspend fun userExists(userId: Int): Boolean = TODO()
    override suspend fun create(email: String, passwordHash: String): User = TODO()
}

/** [bonusPoints]: the balance per user id; users without completed rides are missing. */
private class FakeUserRideRepository(private val bonusPoints: Map<Int, Int>) : RideRepository {
    override suspend fun getTotalBonusPointsByUserId(userId: Int): Int = bonusPoints[userId] ?: 0

    override suspend fun startRideIfNoneInProgress(reservationId: Int, startTime: Instant): Ride? = TODO()
    override suspend fun getRideById(rideId: Int): Ride? = TODO()
    override suspend fun getRideInProgressByUserId(userId: Int): Ride? = TODO()
    override suspend fun getRidesByUserId(userId: Int): List<Ride> = TODO()
    override suspend fun finishRide(id: Int, endTime: Instant, request: RideFinishRequest, bonusPoints: Int): Ride? = TODO()
}

// ==================== 2. Service: UserService ====================

class UserServiceTest {
    private val service = UserService(
        FakeUserRepository(testUser(), testUser(OTHER_USER_ID)),
        FakeUserRideRepository(mapOf(USER_ID to 48))
    )

    // ---------- getUser ----------

    @Test
    fun `gebruiker ophalen geeft die gebruiker`() = runBlocking {
        assertEquals(testUser(), service.getUser(USER_ID))
    }

    @Test
    fun `onbekende gebruiker geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.getUser(999) }
        Unit
    }

    // ---------- getBonusPoints ----------

    @Test
    fun `saldo is de som uit de repository`() = runBlocking {
        assertEquals(48, service.getBonusPoints(USER_ID))
    }

    @Test
    fun `gebruiker zonder afgeronde ritten heeft saldo 0`() = runBlocking {
        assertEquals(0, service.getBonusPoints(OTHER_USER_ID))
    }
}

// ==================== 3. E2E: user routes ====================

class UserRoutesTest {

    // ---------- GET /users/me ----------

    @Test
    fun `eigen gebruiker opvragen geeft 200 met id en e-mail van de ingelogde gebruiker`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        val response = client.get("/users/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(UserResponse(OTHER_USER_ID, "eigenaar$OTHER_USER_ID@carshare.nl", 0), response.body<UserResponse>())
    }

    @Test
    fun `saldo is de som van de bonuspunten van de afgeronde seedritten`() = e2eTest { client ->
        // Seed: gebruiker 3 heeft rit 1 met 48 punten, gebruiker 1 rit 2 met 0 punten
        val token = client.login(3)

        val response = client.get("/users/me") { bearerAuth(token) }

        assertEquals(48, response.body<UserResponse>().bonusPoints)
    }

    @Test
    fun `saldo stijgt na het afronden van een rustige rit`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)
        val now = Clock.System.now()
        val reservation = ExposedReservationRepository().createReservationIfAvailable(
            1, OTHER_USER_ID, now - 1.hours, now + 1.hours, ReservationStatus.CONFIRMED, 89.0
        )!!
        val ride = client.post("/reservations/${reservation.id}/rides") { bearerAuth(token) }.body<Ride>()

        client.patch("/rides/${ride.id}/finish") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(RideFinishRequest(12_500.0, 1.0, 1.0, 51.5853, 4.7946))
        }
        val me = client.get("/users/me") { bearerAuth(token) }.body<UserResponse>()

        assertEquals(12, me.bonusPoints)
    }

    @Test
    fun `lopende rit telt niet mee in het saldo`() = e2eTest { client ->
        // Seed: reservering 4 is van gebruiker 3, die al 48 punten heeft
        val token = client.login(3)
        ExposedRideRepository().startRideIfNoneInProgress(4, Clock.System.now())!!

        val me = client.get("/users/me") { bearerAuth(token) }.body<UserResponse>()

        assertEquals(48, me.bonusPoints)
    }

    @Test
    fun `eigen gebruiker opvragen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/users/me").status)
    }
}
