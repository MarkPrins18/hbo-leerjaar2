package services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import config.JwtConfig
import models.User
import java.util.Date
import kotlin.time.Duration.Companion.minutes

class TokenService(private val config: JwtConfig) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()

    fun createToken(user: User): String {
        val expiresAt = System.currentTimeMillis() + config.expirationMinutes.minutes.inWholeMilliseconds
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(user.id.toString())
            .withClaim(EMAIL_CLAIM, user.email)
            .withExpiresAt(Date(expiresAt))
            .sign(algorithm)
    }

    companion object {
        const val EMAIL_CLAIM = "email"
    }
}
