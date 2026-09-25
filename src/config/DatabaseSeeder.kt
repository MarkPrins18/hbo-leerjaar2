package config

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import tables.allTables

fun Application.seedIfNeeded() {
    val config = environment.config
    val seedData = config.property("database.seed-data").getString().toBoolean()

    val seedFilePaths = listOf(
        "/data/seed/owner.sql",
       // "/data/seed/car.sql",
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