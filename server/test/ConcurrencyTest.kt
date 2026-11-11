import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import repositories.ExposedReservationRepository
import repositories.ExposedRideRepository
import shared.models.Reservation
import shared.models.ReservationStatus
import shared.models.Ride
import shared.requests.LoginRequest
import shared.requests.RegisterRequest
import shared.requests.ReservationRequest
import shared.responses.LoginResponse
import shared.responses.UserResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

// ==================== Test data ====================

private const val PARALLEL_REQUESTS = 10

// Seed: car 1 belongs to user 1, so every other user may reserve it
private const val CAR_ID = 1
private const val RENTER_ID = 2

private val start = Instant.parse("2099-06-01T09:00:00Z")
private val end = Instant.parse("2099-06-01T17:00:00Z")

/** Registers a new user and returns its id and token. */
private suspend fun HttpClient.registerAndLogin(email: String): Pair<Int, String> {
    val user = post("/users/register") {
        contentType(ContentType.Application.Json)
        setBody(RegisterRequest(email, SEED_PASSWORD))
    }.body<UserResponse>()
    val token = post("/users/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest(email, SEED_PASSWORD))
    }.body<LoginResponse>().token
    return user.id to token
}

/** Runs [block] [times] times truly in parallel on Dispatchers.IO and returns all results. */
private suspend fun <T> inParallel(times: Int, block: suspend (Int) -> T): List<T> = coroutineScope {
    (0 until times).map { index -> async(Dispatchers.IO) { block(index) } }.awaitAll()
}

/** A confirmed reservation that is active right now; the API only accepts reservations in the future. */
private suspend fun insertActiveReservation(): Reservation {
    val now = Clock.System.now()
    return ExposedReservationRepository().createReservationIfAvailable(
        CAR_ID, RENTER_ID, now - 1.hours, now + 1.hours, ReservationStatus.CONFIRMED
    )!!
}

// ==================== 3. E2E: concurrency ====================

class ConcurrencyTest {

    // ---------- POST /reservations ----------

    @Test
    fun `gelijktijdig reserveren van hetzelfde tijdsblok geeft precies een 201 en de rest 409`() = e2eTest { client ->
        val users = (1..PARALLEL_REQUESTS).map { client.registerAndLogin("racer$it@carshare.nl") }

        val statuses = inParallel(PARALLEL_REQUESTS) { index ->
            client.post("/reservations") {
                bearerAuth(users[index].second)
                contentType(ContentType.Application.Json)
                setBody(ReservationRequest(CAR_ID, start, end))
            }.status
        }
        val reservationsInSlot = users.sumOf { (_, token) ->
            client.get("/reservations") { bearerAuth(token) }.body<List<Reservation>>()
                .count { it.carId == CAR_ID && it.startTime == start }
        }

        assertEquals(1, statuses.count { it == HttpStatusCode.Created }, "statuses: $statuses")
        assertEquals(PARALLEL_REQUESTS - 1, statuses.count { it == HttpStatusCode.Conflict }, "statuses: $statuses")
        assertEquals(1, reservationsInSlot)
    }

    // ---------- POST /reservations/{id}/rides ----------

    @Test
    fun `gelijktijdig een rit starten op dezelfde reservering geeft precies een 201 en de rest 409`() = e2eTest { client ->
        val token = client.login(RENTER_ID)
        val reservation = insertActiveReservation()

        val responses = inParallel(PARALLEL_REQUESTS) {
            client.post("/reservations/${reservation.id}/rides") { bearerAuth(token) }
        }
        val statuses = responses.map { it.status }
        val started = responses.single { it.status == HttpStatusCode.Created }.body<Ride>()
        val active = client.get("/rides/active") { bearerAuth(token) }.body<Ride>()

        assertEquals(1, statuses.count { it == HttpStatusCode.Created }, "statuses: $statuses")
        assertEquals(PARALLEL_REQUESTS - 1, statuses.count { it == HttpStatusCode.Conflict }, "statuses: $statuses")
        assertEquals(started.id, active.id)
    }

    // ---------- ExposedReservationRepository.createReservationIfAvailable ----------

    @Test
    fun `repository maakt bij gelijktijdige aanroepen maar een reservering voor hetzelfde tijdsblok`() = e2eTest { client ->
        val users = (1..PARALLEL_REQUESTS).map { client.registerAndLogin("racer$it@carshare.nl").first }
        val repository = ExposedReservationRepository()

        val created = inParallel(PARALLEL_REQUESTS) { index ->
            repository.createReservationIfAvailable(CAR_ID, users[index], start, end, ReservationStatus.CONFIRMED)
        }
        val stored = users.flatMap { repository.getReservationsByCarAndUser(CAR_ID, it) }

        assertEquals(1, created.count { it != null })
        assertEquals(1, stored.size)
    }

    // ---------- ExposedRideRepository.startRideIfNoneInProgress ----------

    @Test
    fun `repository start bij gelijktijdige aanroepen maar een rit per reservering`() = e2eTest { client ->
        // The app (and thus the database) only starts on the first request
        client.login(RENTER_ID)
        val reservation = insertActiveReservation()
        val repository = ExposedRideRepository()

        val started = inParallel(PARALLEL_REQUESTS) {
            repository.startRideIfNoneInProgress(reservation.id, Clock.System.now())
        }

        assertEquals(1, started.count { it != null })
        assertEquals(started.single { it != null }!!.id, repository.getRideInProgressByUserId(RENTER_ID)?.id)
    }
}
