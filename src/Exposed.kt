import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureExposed() {
    val config = environment.config
    Database.connect(
        url = config.property("database.url").getString(),
        driver = "com.mysql.cj.jdbc.Driver",
        user = config.property("database.user").getString(),
        password = config.property("database.password").getString(),
    )
}