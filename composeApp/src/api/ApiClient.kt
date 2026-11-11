package nl.carsharing.app.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.DefaultJson
import io.ktor.serialization.kotlinx.json.json

/** Emulator alias for the host machine, where the server listens on port 8080. */
const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"

/**
 * Creates the HTTP client for the Car Sharing API. Uses the same [DefaultJson] as the
 * server's `json()`, so the sealed `Car` is read via the `"type"` discriminator.
 */
fun createApiClient(baseUrl: String = DEFAULT_BASE_URL): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(DefaultJson)
    }
    defaultRequest {
        url(baseUrl.trimEnd('/') + "/")
    }
}
