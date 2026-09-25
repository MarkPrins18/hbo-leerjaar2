package config

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.connectToDatabase(): Boolean {
    val config = environment.config
    val databaseUrl = config.property("database.url").getString()
    val databaseUser = config.property("database.user").getString()
    val databasePassword = config.property("database.password").getString()

    return try {
        Database.connect(
            url = databaseUrl,
            driver = "com.mysql.cj.jdbc.Driver",
            user = databaseUser,
            password = databasePassword,
        )

        transaction {
            exec("SELECT 1")
        }

        log.info("Succesvol verbonden met database '$databaseUrl' als gebruiker '$databaseUser'.")
        true
    } catch (exception: Exception) {
        log.error("Verbinding met de database mislukt.", exception)
        false
    }
}