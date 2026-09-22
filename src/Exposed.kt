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
    try {
        Database.connect(
            url = config.property("database.url").getString(),
            driver = "com.mysql.cj.jdbc.Driver",
            user = config.property("database.user").getString(),
            password = config.property("database.password").getString(),
        )
    } catch (_: Exception) {
        // Geen database beschikbaar, app start wel door.
        //fix exception
    }

    try {
        transaction {
            SchemaUtils.create(OwnerTable, CarTable, IceCarTable, BevCarTable, FcevCarTable)
        }
    } catch (_: Exception) {
        // Schema aanmaken mislukt.
        //fix logging
    }
}