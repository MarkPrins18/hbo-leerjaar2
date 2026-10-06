import config.jwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import services.TokenService
import services.UnauthorizedException

const val JWT_AUTH = "auth-jwt"

fun Application.configureAuthentication() {
    val config = jwtConfig()
    val tokenService = TokenService(config)

    install(Authentication) {
        jwt(JWT_AUTH) {
            realm = config.realm
            verifier(tokenService.verifier)
            validate { credential ->
                credential.payload.subject?.toIntOrNull()?.let { JWTPrincipal(credential.payload) }
            }
            challenge { _, _ ->
                throw UnauthorizedException("Ontbrekende, ongeldige of verlopen token")
            }
        }
    }
}

fun Route.requireAuthentication(build: Route.() -> Unit): Route =
    authenticate(JWT_AUTH, build = build)

fun ApplicationCall.authenticatedUserId(): Int =
    principal<JWTPrincipal>()?.subject?.toIntOrNull() ?: throw UnauthorizedException()
