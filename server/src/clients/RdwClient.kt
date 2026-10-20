package clients

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import models.rdw.RdwFuelDto
import models.rdw.RdwVehicleDto
import mappers.normalizeLicensePlate

class RdwClient(private val appToken: String) : JsonApiClient(), VehicleRegistry {

    override suspend fun getVehicle(licensePlate: String): RdwVehicleDto? {
        val response: List<RdwVehicleDto> = httpClient.get("https://opendata.rdw.nl/resource/m9d7-ebf2.json") {
            parameter("kenteken", normalizeLicensePlate(licensePlate))
            header("X-App-Token", appToken)
        }.body()
        return response.firstOrNull()
    }

    override suspend fun getFuel(licensePlate: String): List<RdwFuelDto> {
        val result: List<RdwFuelDto> = httpClient.get(
            "https://opendata.rdw.nl/resource/8ys7-d773.json"
        ) {
            parameter("kenteken", normalizeLicensePlate(licensePlate))
            header("X-App-Token", appToken)
        }.body()

        return result
    }
}
