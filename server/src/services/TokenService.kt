package services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.RegisteredClaims
import config.JwtConfig
import models.User
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

class TokenService(val config: JwtConfig) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()

    fun createToken(user: User): String {
        val expiresAt = Clock.System.now() + config.expirationMinutes.minutes
        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(user.id.toString())
            .withClaim(EMAIL_CLAIM, user.email)
            .withClaim(RegisteredClaims.EXPIRES_AT, expiresAt.epochSeconds)
            .sign(algorithm)
    }

    companion object {
        const val EMAIL_CLAIM = "email"
    }
}
