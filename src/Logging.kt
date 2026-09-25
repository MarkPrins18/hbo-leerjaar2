import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

// Issue #31 - Logging
//
// LET OP: dit voegt de Ktor CallLogging-plugin toe via de Amper-catalogusnaam
// "$ktor.server.callLogging" in module.yaml. Die naam is een educated guess naar
// analogie van de al aanwezige "$ktor.server.statusPages" en
// "$ktor.server.contentNegotiation" -- ik kon dit niet compileren in deze
// omgeving (geen toegang tot Maven Central/JetBrains-repositories). Controleer
// dit dus met `./kotlin build` voordat je dit inlevert. Lukt de catalogusnaam
// niet, voeg dan de dependency `io.ktor:ktor-server-call-logging` handmatig toe
// aan module.yaml (of libs.versions.toml + module.yaml).
fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val method = call.request.httpMethod.value
            val uri = call.request.uri
            "$status $method $uri"
        }
    }
}