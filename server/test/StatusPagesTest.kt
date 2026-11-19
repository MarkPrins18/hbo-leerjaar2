import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import shared.responses.ErrorResponse
import kotlin.test.Test
import kotlin.test.assertEquals

// ==================== 3. E2E: error responses ====================

class StatusPagesRoutesTest {

    // ---------- onbekende route ----------

    @Test
    fun `onbekende route geeft 404 als ErrorResponse`() = e2eTest { client ->
        val response = client.get("/bestaat-niet")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ErrorResponse(404, "Deze route bestaat niet"), response.body<ErrorResponse>())
    }

    // ---------- verkeerde methode ----------

    @Test
    fun `verkeerde methode op een bestaande route geeft 405 als ErrorResponse`() = e2eTest { client ->
        val token = client.login(1)

        val response = client.delete("/users/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.MethodNotAllowed, response.status)
        assertEquals(
            ErrorResponse(405, "Deze methode is niet toegestaan op deze route"),
            response.body<ErrorResponse>()
        )
    }

    // ---------- ResourceNotFoundException ----------

    @Test
    fun `ResourceNotFoundException houdt zijn eigen melding`() = e2eTest { client ->
        val response = client.get("/cars/999")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(ErrorResponse(404, "Auto 999 niet gevonden"), response.body<ErrorResponse>())
    }
}
