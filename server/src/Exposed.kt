import config.*
import io.ktor.server.application.*

fun Application.configureExposed() {
    connectToDatabase()
    syncDatabaseSchema()
    seedIfNeeded()
}
