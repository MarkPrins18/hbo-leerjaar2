package clients

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto

class RdwClient(private val appToken: String) {

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getVehicle(licensePlate: String): RdwVehicleDto? {
        val response: List<RdwVehicleDto> = httpClient.get("https://opendata.rdw.nl/resource/m9d7-ebf2.json") {
            parameter("kenteken", normalize(licensePlate))
            header("X-App-Token", appToken)
        }.body()
        return response.firstOrNull()
    }

    suspend fun getFuel(licensePlate: String): List<RdwFuelDto> {
        val result: List<RdwFuelDto> = httpClient.get(
            "https://opendata.rdw.nl/resource/8ys7-d773.json"
        ) {
            parameter("kenteken", normalize(licensePlate))
            header("X-App-Token", appToken)
        }.body()

        println(result)

        return result
    }

    /*suspend fun getFuel(licensePlate: String): List<RdwFuelDto> {
        return httpClient.get("https://opendata.rdw.nl/resource/8ys7-d773.json") {
            parameter("kenteken", normalize(licensePlate))
            header("X-App-Token", appToken)
        }.body()
    }*/

    private fun normalize(licensePlate: String): String =
        licensePlate.replace("-", "").uppercase()
}