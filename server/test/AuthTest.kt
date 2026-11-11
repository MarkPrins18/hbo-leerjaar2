import config.JwtConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import models.User
import org.mindrot.jbcrypt.BCrypt
import repositories.UserRepository
import services.AuthService
import services.ConflictException
import services.TokenService
import services.UnauthorizedException
import services.UserService
import services.ValidationException
import services.validate
import shared.requests.LoginRequest
import shared.requests.RegisterRequest
import shared.responses.ErrorResponse
import shared.responses.LoginResponse
import shared.responses.UserResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

// ==================== Test data ====================

private const val EXISTING_USER_ID = 1
private const val EXISTING_EMAIL = "eigenaar1@carshare.nl"
private const val VALID_PASSWORD = "Welkom123!"
private const val EMAIL_DOMAIN = "@carshare.nl"

private fun registerRequest(email: String = "nieuw@carshare.nl", password: String = VALID_PASSWORD) =
    RegisterRequest(email, password)

/** Lowest bcrypt cost, so the service tests stay fast. */
private fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(4))

private fun existingUser() = User(EXISTING_USER_ID, EXISTING_EMAIL, hash(VALID_PASSWORD))

private fun tokenService() = TokenService(
    JwtConfig(
        secret = TEST_JWT_SECRET,
        issuer = "carshare",
        audience = "carshare-users",
        realm = "carshare",
        expirationMinutes = 60
    )
)

// ==================== Fakes ====================

private class FakeAuthUserRepository(vararg users: User) : UserRepository {
    private val users = users.toMutableList()

    override suspend fun findByEmail(email: String): User? = users.find { it.email == email }

    override suspend fun create(email: String, passwordHash: String): User =
        User((users.maxOfOrNull { it.id } ?: 0) + 1, email, passwordHash).also { users += it }

    fun count(): Int = users.size

    override suspend fun findById(userId: Int): User? = TODO()
    override suspend fun findAll(): List<User> = TODO()
    override suspend fun userExists(userId: Int): Boolean = TODO()
}

// ==================== 1. Validation: RegisterRequest ====================

class UserValidationTest {

    // ---------- email ----------

    @Test
    fun `geldig e-mailadres en wachtwoord worden geaccepteerd`() {
        registerRequest().validate()
    }

    @Test
    fun `e-mailadres met spaties eromheen wordt geaccepteerd`() {
        registerRequest(email = "  nieuw@carshare.nl  ").validate()
    }

    @Test
    fun `e-mailadres zonder apenstaartje geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(email = "nieuw.carshare.nl").validate() }
    }

    @Test
    fun `e-mailadres zonder domeinextensie geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(email = "nieuw@carshare").validate() }
    }

    @Test
    fun `leeg e-mailadres geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(email = "").validate() }
    }

    @Test
    fun `e-mailadres van precies 255 tekens is toegestaan`() {
        val email = "a".repeat(255 - EMAIL_DOMAIN.length) + EMAIL_DOMAIN

        registerRequest(email = email).validate()
    }

    @Test
    fun `e-mailadres van 256 tekens geeft ValidationException`() {
        val email = "a".repeat(256 - EMAIL_DOMAIN.length) + EMAIL_DOMAIN

        assertFailsWith<ValidationException> { registerRequest(email = email).validate() }
    }

    // ---------- password ----------

    @Test
    fun `wachtwoord van precies 8 tekens is toegestaan`() {
        registerRequest(password = "Welkom1!").validate()
    }

    @Test
    fun `wachtwoord van 7 tekens geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(password = "Welko1!").validate() }
    }

    @Test
    fun `leeg wachtwoord geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(password = "").validate() }
    }

    @Test
    fun `wachtwoord zonder hoofdletter geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(password = "welkom123!").validate() }
    }

    @Test
    fun `wachtwoord zonder kleine letter geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(password = "WELKOM123!").validate() }
    }

    @Test
    fun `wachtwoord zonder cijfer geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(password = "Welkomwelkom!").validate() }
    }

    @Test
    fun `wachtwoord zonder speciaal teken geeft ValidationException`() {
        assertFailsWith<ValidationException> { registerRequest(password = "Welkom1234").validate() }
    }

    @Test
    fun `wachtwoord van precies 72 bytes is toegestaan`() {
        val password = "Aa1!" + "a".repeat(68)

        registerRequest(password = password).validate()
    }

    @Test
    fun `wachtwoord van 73 bytes geeft ValidationException`() {
        val password = "Aa1!" + "a".repeat(69)

        assertFailsWith<ValidationException> { registerRequest(password = password).validate() }
    }

    @Test
    fun `wachtwoord onder 72 tekens maar boven 72 bytes geeft ValidationException`() {
        // 'é' is two bytes in UTF-8: 4 + 35 * 2 = 74 bytes in 39 characters
        val password = "Aa1!" + "é".repeat(35)

        assertFailsWith<ValidationException> { registerRequest(password = password).validate() }
    }
}

// ==================== 2. Service: UserService and AuthService ====================

class UserRegistrationServiceTest {
    private val repository = FakeAuthUserRepository(existingUser())
    private val service = UserService(repository)

    // ---------- register ----------

    @Test
    fun `registreren maakt een gebruiker met genormaliseerd e-mailadres en gehasht wachtwoord`() = runBlocking {
        val user = service.register(registerRequest(email = "  Nieuw@CarShare.nl "))

        assertEquals("nieuw@carshare.nl", user.email)
        assertNotEquals(VALID_PASSWORD, user.passwordHash)
        assertTrue(BCrypt.checkpw(VALID_PASSWORD, user.passwordHash))
        assertEquals(2, repository.count())
    }

    @Test
    fun `registreren met een bestaand e-mailadres geeft ConflictException`() = runBlocking {
        assertFailsWith<ConflictException> { service.register(registerRequest(email = EXISTING_EMAIL)) }
        assertEquals(1, repository.count())
    }

    @Test
    fun `bestaand e-mailadres met hoofdletters geeft ook ConflictException`() = runBlocking {
        assertFailsWith<ConflictException> { service.register(registerRequest(email = "Eigenaar1@CarShare.NL")) }
        assertEquals(1, repository.count())
    }

    @Test
    fun `registreren met een zwak wachtwoord geeft ValidationException zonder gebruiker aan te maken`() = runBlocking {
        assertFailsWith<ValidationException> { service.register(registerRequest(password = "zwak")) }
        assertEquals(1, repository.count())
    }
}

class AuthServiceTest {
    private val tokenService = tokenService()
    private val service = AuthService(FakeAuthUserRepository(existingUser()), tokenService)

    // ---------- login ----------

    @Test
    fun `inloggen met juist wachtwoord geeft een token voor die gebruiker`() = runBlocking {
        val token = service.login(LoginRequest(EXISTING_EMAIL, VALID_PASSWORD))

        assertEquals(EXISTING_USER_ID.toString(), tokenService.verifier.verify(token).subject)
    }

    @Test
    fun `inloggen met e-mailadres in hoofdletters en spaties werkt`() = runBlocking {
        val token = service.login(LoginRequest(" EIGENAAR1@carshare.nl ", VALID_PASSWORD))

        assertEquals(EXISTING_USER_ID.toString(), tokenService.verifier.verify(token).subject)
    }

    @Test
    fun `inloggen met fout wachtwoord geeft UnauthorizedException`() = runBlocking {
        assertFailsWith<UnauthorizedException> { service.login(LoginRequest(EXISTING_EMAIL, "Fout123!")) }
        Unit
    }

    @Test
    fun `inloggen met onbekend e-mailadres geeft UnauthorizedException`() = runBlocking {
        assertFailsWith<UnauthorizedException> { service.login(LoginRequest("onbekend@carshare.nl", VALID_PASSWORD)) }
        Unit
    }
}

// ==================== 3. E2E: register and login routes ====================

class AuthRoutesTest {
    private val newEmail = "nieuw@carshare.nl"

    private suspend fun HttpClient.register(body: String): HttpResponse =
        post("/users/register") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    private suspend fun HttpClient.loginWith(email: String, password: String): HttpResponse =
        post("/users/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }

    private fun registerBody(email: String = newEmail, password: String = VALID_PASSWORD) =
        """{ "email": "$email", "password": "$password" }"""

    // ---------- POST /users/register ----------

    @Test
    fun `registreren geeft 201 met id en e-mail`() = e2eTest { client ->
        val response = client.register(registerBody())

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(newEmail, response.body<UserResponse>().email)
    }

    @Test
    fun `registreren met ongeldig e-mailadres geeft 400 als ErrorResponse`() = e2eTest { client ->
        val response = client.register(registerBody(email = "geen-email"))

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(400, response.body<ErrorResponse>().status)
    }

    @Test
    fun `registreren met zwak wachtwoord geeft 400`() = e2eTest { client ->
        assertEquals(HttpStatusCode.BadRequest, client.register(registerBody(password = "zwak")).status)
    }

    @Test
    fun `registreren zonder wachtwoord geeft 400`() = e2eTest { client ->
        assertEquals(HttpStatusCode.BadRequest, client.register("""{ "email": "$newEmail" }""").status)
    }

    @Test
    fun `registreren met een bestaand e-mailadres geeft 409 als ErrorResponse`() = e2eTest { client ->
        val response = client.register(registerBody(email = EXISTING_EMAIL))

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertEquals(409, response.body<ErrorResponse>().status)
    }

    // ---------- POST /users/login ----------

    @Test
    fun `geregistreerde gebruiker logt in en kan met het token zichzelf opvragen`() = e2eTest { client ->
        val registered = client.register(registerBody()).body<UserResponse>()

        val login = client.loginWith(newEmail, VALID_PASSWORD)
        val token = login.body<LoginResponse>().token
        val me = client.get("/users/me") { bearerAuth(token) }

        assertEquals(HttpStatusCode.OK, login.status)
        assertEquals(HttpStatusCode.OK, me.status)
        assertEquals(registered, me.body<UserResponse>())
    }

    @Test
    fun `inloggen met fout wachtwoord geeft 401 als ErrorResponse`() = e2eTest { client ->
        val response = client.loginWith(EXISTING_EMAIL, "Fout123!")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponse(401, "Onjuist e-mailadres of wachtwoord"), response.body<ErrorResponse>())
    }

    @Test
    fun `inloggen als onbekende gebruiker geeft 401 als ErrorResponse`() = e2eTest { client ->
        val response = client.loginWith("onbekend@carshare.nl", VALID_PASSWORD)

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(ErrorResponse(401, "Onjuist e-mailadres of wachtwoord"), response.body<ErrorResponse>())
    }
}
