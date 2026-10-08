package config

import io.ktor.server.application.*

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationMinutes: Long
)

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
