import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import models.User
import repositories.UserRepository
import services.ResourceNotFoundException
import services.UserService
import shared.responses.UserResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// ==================== Test data ====================

private const val USER_ID = 1
private const val OTHER_USER_ID = 2

private fun testUser(id: Int = USER_ID) = User(id, "eigenaar$id@carshare.nl", "hash")

// ==================== Fakes ====================

private class FakeUserRepository(vararg users: User) : UserRepository {
    private val users = users.associateBy { it.id }

    override suspend fun findById(userId: Int): User? = users[userId]

    override suspend fun findByEmail(email: String): User? = TODO()
    override suspend fun findAll(): List<User> = TODO()
    override suspend fun userExists(userId: Int): Boolean = TODO()
    override suspend fun create(email: String, passwordHash: String): User = TODO()
}

// ==================== 2. Service: UserService ====================

class UserServiceTest {
    private val service = UserService(FakeUserRepository(testUser(), testUser(OTHER_USER_ID)))

    // ---------- getUser ----------

    @Test
    fun `gebruiker ophalen geeft die gebruiker`() = runBlocking {
        assertEquals(testUser(), service.getUser(USER_ID))
    }

    @Test
    fun `onbekende gebruiker geeft ResourceNotFoundException`() = runBlocking {
        assertFailsWith<ResourceNotFoundException> { service.getUser(999) }
        Unit
    }
}

// ==================== 3. E2E: user routes ====================

class UserRoutesTest {

    // ---------- GET /users/me ----------

    @Test
    fun `eigen gebruiker opvragen geeft 200 met id en e-mail van de ingelogde gebruiker`() = e2eTest { client ->
        val token = client.login(OTHER_USER_ID)

        val response = client.get("/users/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(UserResponse(OTHER_USER_ID, "eigenaar$OTHER_USER_ID@carshare.nl"), response.body<UserResponse>())
    }

    @Test
    fun `eigen gebruiker opvragen zonder token geeft 401`() = e2eTest { client ->
        assertEquals(HttpStatusCode.Unauthorized, client.get("/users/me").status)
    }
}
