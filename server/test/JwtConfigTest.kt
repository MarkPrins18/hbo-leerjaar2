import config.JWT_SECRET_MIN_LENGTH
import config.JwtConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// ==================== Test data ====================

private fun jwtConfig(secret: String) = JwtConfig(
    secret = secret,
    issuer = "carshare",
    audience = "carshare-users",
    realm = "carshare",
    expirationMinutes = 60
)

// ==================== 1. Validation: JWT secret ====================

class JwtConfigTest {

    @Test
    fun `secret van precies 32 tekens is toegestaan`() {
        val secret = "a".repeat(JWT_SECRET_MIN_LENGTH)

        assertEquals(secret, jwtConfig(secret).secret)
    }

    @Test
    fun `secret van 31 tekens geeft een fout bij het opstarten`() {
        assertFailsWith<IllegalArgumentException> { jwtConfig("a".repeat(JWT_SECRET_MIN_LENGTH - 1)) }
    }

    @Test
    fun `ontbrekend secret geeft een fout bij het opstarten`() {
        assertFailsWith<IllegalArgumentException> { jwtConfig("") }
    }
}
