package repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import models.RouteResult
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Locale

/**
 * Issue #10 - Route naar auto / externe routering.
 *
 * Haalt een berekende route (afstand + reistijd) op bij de publieke OSRM-server
 * (https://project-osrm.org/) via een gewone java.net.http.HttpClient -- bewust
 * GEEN nieuwe build-dependency (zoals een Ktor HTTP-client), om het aantal
 * ongeteste aannames in module.yaml/libs.versions.toml te beperken.
 *
 * LET OP: dit gebruikt de publieke DEMO-server van OSRM (bedoeld voor testen/
 * evalueren, niet voor productieverkeer met veel gebruikers -- zie hun fair-use-
 * beleid op https://github.com/Project-OSRM/osrm-backend/wiki/Api-usage-policy).
 * Voor een echte productie-inzet zou je zelf een OSRM-server hosten of een
 * commerciele dienst gebruiken. Kon niet end-to-end getest worden in deze
 * omgeving (geen netwerktoegang naar externe API's hier).
 */
class OsrmRouteRepository(
    private val baseUrl: String = "https://router.project-osrm.org"
) : RouteRepository {

    private val client = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class OsrmResponse(val code: String, val routes: List<OsrmRoute> = emptyList())

    @Serializable
    private data class OsrmRoute(val distance: Double, val duration: Double)

    override suspend fun calculateRoute(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): RouteResult? = withContext(Dispatchers.IO) {
        val coords = String.format(
            Locale.US,
            "%s,%s;%s,%s",
            startLongitude, startLatitude, endLongitude, endLatitude
        )
        val uri = URI("$baseUrl/route/v1/driving/$coords?overview=false")
        val request = HttpRequest.newBuilder(uri).GET().build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) return@withContext null

        val parsed = json.decodeFromString<OsrmResponse>(response.body())
        if (parsed.code != "Ok" || parsed.routes.isEmpty()) return@withContext null

        val route = parsed.routes.first()
        RouteResult(distanceMeters = route.distance, durationSeconds = route.duration)
    }
}
