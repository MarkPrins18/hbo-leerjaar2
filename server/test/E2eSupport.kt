import config.PhotoStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.testing.testApplication
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import requests.LoginRequest
import responses.LoginResponse
import kotlin.uuid.Uuid

const val SEED_PASSWORD = "Welkom123!"

/**
 * Starts the full app from application.yaml, but with its own in-memory database and
 * photo directory, so tests never touch the local data in resources/data/database/.
 * The app resets and seeds that database on start, so every test begins with the seed data.
 */
fun e2eTest(block: suspend (HttpClient) -> Unit) {
    val name = "e2e-${Uuid.random()}"
    val photoDirectory = Path(SystemTemporaryDirectory, name)

    try {
        testApplication {
            environment {
                config = ApplicationConfig("application.yaml").mergeWith(
                    MapApplicationConfig(
                        "database.url" to "jdbc:h2:mem:$name;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                        "photos.directory" to photoDirectory.toString()
                    )
                )
            }
            block(createClient { install(ContentNegotiation) { json() } })
        }
    } finally {
        PhotoStorage(photoDirectory).clear()
        SystemFileSystem.delete(photoDirectory, mustExist = false)
    }
}

/** Logs in as seed user `eigenaar[userId]@carshare.nl` and returns the JWT. */
suspend fun HttpClient.login(userId: Int): String =
    post("/users/login") {
        contentType(ContentType.Application.Json)
        setBody(LoginRequest("eigenaar$userId@carshare.nl", SEED_PASSWORD))
    }.body<LoginResponse>().token
