package config

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import tables.PhotoTable
import tables.allTables

fun Application.seedIfNeeded() {
    val config = environment.config
    val seedData = config.property("database.seed-data").getString().toBoolean()

    val seedFilePaths = listOf(
        "/data/seed/users.sql",
        "/data/seed/car.sql",
        "/data/seed/photo.sql",
        "/data/seed/location.sql",
        "/data/seed/rental_terms.sql",
        "/data/seed/reservation.sql",
        "/data/seed/ride.sql",
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

                val photoStorage = photoStorage()
                PhotoTable.select(PhotoTable.fileName).forEach { row ->
                    val fileName = row[PhotoTable.fileName]
                    val photoFile = object {}.javaClass.getResource("/data/seed/photos/$fileName")
                        ?: error("Seed foto '$fileName' niet gevonden.")

                    photoStorage.save(fileName, photoFile.readBytes())
                }

                log.info("Seed data succesvol toegevoegd.")
            }
        }
    } catch (exception: Exception) {
        log.error("Seed data toevoegen mislukt.", exception)
    }
}