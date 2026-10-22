package clients

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

abstract class JsonApiClient {

    protected val json = Json { ignoreUnknownKeys = true }

    // Created on the first request, so tests that never call the API don't open a client
    protected val httpClient by lazy {
        HttpClient(CIO) {
            expectSuccess = true
            // Gemini often needs more than the default 15 seconds
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }
            install(ContentNegotiation) {
                json(json)
            }
        }
    }
}
