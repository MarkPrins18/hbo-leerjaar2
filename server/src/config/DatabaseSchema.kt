package config

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import tables.allTables

fun Application.syncDatabaseSchema() {
    val config = environment.config
    val resetSchema = config.property("database.reset-schema").getString().toBoolean()

    transaction {
        if (resetSchema) {
            SchemaUtils.drop(*allTables.toTypedArray())
            photoStorage().clear()
            log.warn("Database schema verwijderd omdat database.reset-schema=true is.")
        }

        SchemaUtils.create(*allTables.toTypedArray())
    }

    log.info("Database-transactie succesvol uitgevoerd en schema gecontroleerd.")
}
