import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import tables.OwnerTable
import tables.CarTable
import tables.IceCarTable
import tables.BevCarTable
import tables.FcevCarTable

fun Application.configureExposed() {
    val config = environment.config
    val databaseUrl = config.property("database.url").getString()
    val databaseUser = config.property("database.user").getString()

    try {
        Database.connect(
            url = databaseUrl,
            driver = "com.mysql.cj.jdbc.Driver",
            user = databaseUser,
            password = config.property("database.password").getString(),
        )
        log.info("Succesvol verbonden met database '$databaseUrl' als gebruiker '$databaseUser'.")

        try {
            transaction {
                val resetSchema = config.property("database.reset-schema").getString().toBoolean()

                if (resetSchema) {
                    SchemaUtils.drop(IceCarTable, BevCarTable, FcevCarTable, CarTable, OwnerTable)
                    log.warn("Database schema verwijderd omdat database.reset-schema=true is.")
                }

                SchemaUtils.create(OwnerTable, CarTable, IceCarTable, BevCarTable, FcevCarTable)
            }
            log.info("Database-transactie succesvol uitgevoerd en schema gecontroleerd.")
        } catch (exception: Exception) {
            log.error("Schema aanmaken mislukt.", exception)
        }
    } catch (exception: Exception) {
        log.error("Verbinding met de database mislukt. Schema wordt niet aangemaakt.", exception)
    }
}