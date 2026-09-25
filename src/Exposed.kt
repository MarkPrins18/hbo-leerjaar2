import config.*
import io.ktor.server.application.*

fun Application.configureExposed() {
    if (!connectToDatabase()) return

    if (!syncDatabaseSchema()) return

    seedIfNeeded()
}