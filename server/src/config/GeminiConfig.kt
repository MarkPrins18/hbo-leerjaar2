package config

import clients.GeminiClient
import io.ktor.server.application.*

fun Application.configureGemini(): GeminiClient {
    val apiKey = environment.config.property("gemini.apiKey").getString()
    val model = environment.config.property("gemini.model").getString()
    return GeminiClient(apiKey, model)
}
