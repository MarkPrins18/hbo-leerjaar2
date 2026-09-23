import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import tables.allTables

fun Application.configureExposed() {
    val connected = connectToDatabase()
    if (!connected) return

    val schemaReady = syncDatabaseSchema()
    if (!schemaReady) return

    seedIfNeeded()
}

private fun Application.connectToDatabase(): Boolean {
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
        log.info("Succesvol verbonden met database '$databaseUrl' als gebruiker '$databaseUser'.") //test logging
        true
    } catch (exception: Exception) {
        log.error("Verbinding met de database mislukt. Schema wordt niet aangemaakt.", exception)
        false
    }
}

private fun Application.syncDatabaseSchema(): Boolean {
    val config = environment.config
    val resetSchema = config.property("database.reset-schema").getString().toBoolean()

    return try {
        transaction {

            if (resetSchema) {
                SchemaUtils.drop(*allTables.toTypedArray())
                log.warn("Database schema verwijderd omdat database.reset-schema=true is.")
            }

            SchemaUtils.create(*allTables.toTypedArray())
        }
        log.info("Database-transactie succesvol uitgevoerd en schema gecontroleerd.")
        true
    } catch (exception: Exception) {
        log.error("Schema aanmaken mislukt.", exception)
        false
    }
}

private fun Application.seedIfNeeded() {
    val config = environment.config
    val seedData = config.property("database.seed-data").getString().toBoolean()
    val seedFilePaths = listOf(
        "/data/seed/owner.sql",
        "/data/seed/car.sql",
    )

    if (!seedData) return

    try {
        transaction {
            if (allTables.all { it.selectAll().empty() }) {
                seedFilePaths.forEach { path ->
                    val seedFile = object {}.javaClass.getResource(path)
                        ?: error("Seed bestand '$path' niet gevonden.")

                    seedFile.readText()
                        .split(";")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .forEach { statement ->
                            exec(statement)
                        }
                }
                log.info("Seed data succesvol toegevoegd.")
            }
        }
    } catch (exception: Exception) {
        log.error("Seed data toevoegen mislukt.", exception)
    }
}