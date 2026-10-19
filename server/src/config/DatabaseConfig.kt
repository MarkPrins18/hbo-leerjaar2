package config

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private const val DATABASE_URL =
    "jdbc:h2:file:./server/resources/data/database/carshare;MODE=MySQL;DATABASE_TO_LOWER=TRUE;TRACE_LEVEL_FILE=0"

fun Application.connectToDatabase(): Boolean {
    val config = environment.config
    val databaseUser = config.property("database.user").getString()
    val databasePassword = config.property("database.password").getString()

    return try {
        Database.connect(
            url = DATABASE_URL,
            driver = "org.h2.Driver",
            user = databaseUser,
            password = databasePassword,
        )

        transaction {
            exec("SELECT 1")
        }

        log.info("Succesvol verbonden met database '$DATABASE_URL' als gebruiker '$databaseUser'.")
        true
    } catch (exception: Exception) {
        log.error("Verbinding met de database mislukt.", exception)
        false
    }
}