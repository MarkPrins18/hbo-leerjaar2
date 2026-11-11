package config

import io.ktor.server.application.*

internal const val JWT_SECRET_MIN_LENGTH = 32

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationMinutes: Long
) {
    init {
        require(secret.length >= JWT_SECRET_MIN_LENGTH) {
            "JWT_SECRET ontbreekt of is korter dan $JWT_SECRET_MIN_LENGTH tekens. " +
                "Zet de omgevingsvariabele JWT_SECRET voordat je de server start."
        }
    }
}

fun Application.jwtConfig(): JwtConfig {
    val config = environment.config
    return JwtConfig(
        secret = config.property("jwt.secret").getString(),
        issuer = config.property("jwt.issuer").getString(),
        audience = config.property("jwt.audience").getString(),
        realm = config.property("jwt.realm").getString(),
        expirationMinutes = config.property("jwt.expiration-minutes").getString().toLong()
    )
}
