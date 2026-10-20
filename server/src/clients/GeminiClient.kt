package clients

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import models.*
import models.gemini.*
import services.ExternalServiceException

class GeminiClient(private val apiKey: String, private val model: String) : CostEstimator {

    private val json = Json { ignoreUnknownKeys = true }

    private val httpClient = HttpClient(CIO) {
        expectSuccess = true
        // Gemini often needs more than the default 15 seconds
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    // Forces Gemini to answer with exactly this JSON object
    private val costEstimateSchema = GeminiSchema(
        type = "OBJECT",
        properties = mapOf(
            "roadTaxPerQuarter" to GeminiSchema(type = "NUMBER", nullable = true, description = "MRB per kwartaal in euro"),
            "consumptionCombined" to GeminiSchema(type = "NUMBER", nullable = true, description = "Gecombineerd verbruik per 100 km")
        ),
        required = listOf("roadTaxPerQuarter", "consumptionCombined")
    )

    override suspend fun estimateCarCosts(car: Car): CarCostEstimate {
        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = buildPrompt(car))))),
            generationConfig = GeminiGenerationConfig(
                responseMimeType = "application/json",
                responseSchema = costEstimateSchema
            )
        )

        val response: GeminiResponse = httpClient.post(
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        ) {
            header("x-goog-api-key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw ExternalServiceException("Gemini gaf geen antwoord")

        return json.decodeFromString<CarCostEstimate>(text)
    }

    private fun buildPrompt(car: Car): String {
        val fuelType = when (car) {
            is ICECar -> when (car.fuelType) {
                FuelType.PETROL -> "Benzine"
                FuelType.DIESEL -> "Diesel"
                FuelType.LPG -> "LPG"
            }
            is BEVCar -> "Elektriciteit"
            is FCEVCar -> "Waterstof"
        }
        val (drivetrain, consumptionUnit) = when (car) {
            is ICECar -> "verbrandingsmotor" to "liter per 100 km"
            is BEVCar -> "volledig elektrisch, accu ${car.batteryCapacityKWh} kWh" to "kWh per 100 km"
            is FCEVCar -> "waterstof (brandstofcel)" to "kg waterstof per 100 km"
        }

        return """
            Geef een schatting voor de volgende Nederlandse auto:
            - Merk: ${car.brand}
            - Model: ${car.model}
            - Uitvoering: ${car.trim ?: "onbekend"}
            - Bouwjaar: ${car.productionYear}
            - Voertuigsoort: ${car.vehicleType}
            - Aandrijving: $drivetrain
            - Brandstofsoort (RDW): $fuelType
            - Massa rijklaar (RDW): ${car.readyToDriveWeightKg} kg
            - CO2-uitstoot gecombineerd: ${car.co2EmissionCombined?.let { "$it g/km" } ?: "onbekend"}

            Bepaal:
            - roadTaxPerQuarter: de motorrijtuigenbelasting (MRB) per kwartaal in euro voor een particuliere eigenaar in de provincie Noord-Brabant, volgens de tarieven van 2026 (inclusief de provinciale opcenten van 2026).
            - consumptionCombined: het gecombineerde verbruik in $consumptionUnit.

            Regels:
            - Antwoord alleen met getallen, zonder eenheden.
            - Als je een bereik weet, geef dan het midden van dat bereik als één getal.
            - Als je een waarde niet kunt bepalen, geef dan null.

            Gebruik ALLEEN de onderstaande regels om de wegenbelasting te berekenen.
        """.trimIndent() + "\n<regels>\n$ROAD_TAX_RULES_2026\n</regels>"
    }
}
