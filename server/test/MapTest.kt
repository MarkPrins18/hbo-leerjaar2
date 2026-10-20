import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import models.Location
import repositories.LocationRepository
import services.LocationService
import services.ResourceNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

// ==================== Test data ====================

private const val CAR_ID = 10

private val location = Location(
    id = 1, carId = CAR_ID, latitude = 51.5853, longitude = 4.7946, speed = null,
    timestamp = Instant.parse("2026-09-29T16:00:00Z")
)

// ==================== Fakes ====================

private class FakeLocationRepository(vararg locations: Location) : LocationRepository {
    private val locations = locations.associateBy { it.carId }

    override suspend fun getLatestLocationByCarId(carId: Int): Location? = locations[carId]
}

// ==================== 2. Service: LocationService ====================

class LocationServiceTest {
    private val service = LocationService(FakeLocationRepository(location))

    // ---------- getMapsUrl ----------

    @Test
    fun `maps-link bevat de laatst bekende locatie als bestemming`() = runBlocking {
        val url = service.getMapsUrl(CAR_ID)

        assertEquals("https://www.google.com/maps/dir/?api=1&destination=51.5853,4.7946", url)
    }

    @Test
    fun `auto zonder locatie geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.getMapsUrl(999) }
        Unit
    }
}

// ==================== 3. E2E: map route ====================

class MapRoutesTest {
    private val seedCarId = 1
    private val seedUserId = 1

    private suspend fun HttpClient.maps(carId: Int, token: String? = null): HttpResponse =
        get("/cars/$carId/maps") { token?.let { bearerAuth(it) } }

    // ---------- GET /cars/{carId}/maps ----------

    @Test
    fun `ingelogde gebruiker krijgt de maps-link van een auto`() = e2eTest { client ->
        val token = client.login(seedUserId)

        val response = client.maps(seedCarId, token)

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("https://www.google.com/maps/dir/?api=1&destination=51.64361,4.86044", response.bodyAsText())
    }

    @Test
    fun `maps-link zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.maps(seedCarId).status)
    }

    @Test
    fun `auto zonder locatie geeft 404`() = e2eTest { client ->
        val token = client.login(seedUserId)

        assertEquals(HttpStatusCode.NotFound, client.maps(999, token).status)
    }
}
