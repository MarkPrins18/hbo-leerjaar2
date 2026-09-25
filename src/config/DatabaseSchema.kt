package config

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import tables.allTables

fun Application.syncDatabaseSchema(): Boolean {
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