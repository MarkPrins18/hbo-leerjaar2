package routes

import authenticatedUserId
import io.ktor.http.HttpStatusCode
import requireAuthentication
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import shared.requests.LoginRequest
import shared.requests.RegisterRequest
import shared.responses.LoginResponse
import mappers.toResponse
import services.AuthService
import services.UserService

fun Route.userRoutes(userService: UserService, authService: AuthService) {
    route("/users") {
        // TODO: temporary endpoint for testing, remove before release
        get {
            val users = userService.getAllUsers().map { it.toResponse(userService.getBonusPoints(it.id)) }
            call.respond(HttpStatusCode.OK, users)
        }
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val user = userService.register(request)
            // A new account has no rides yet
            call.respond(HttpStatusCode.Created, user.toResponse(bonusPoints = 0))
        }
        post("/login") {
            val request = call.receive<LoginRequest>()
            val token = authService.login(request)
            call.respond(HttpStatusCode.OK, LoginResponse(token))
        }
        requireAuthentication {
            get("/me") {
                val userId = call.authenticatedUserId()
                val user = userService.getUser(userId)
                call.respond(HttpStatusCode.OK, user.toResponse(userService.getBonusPoints(user.id)))
            }
        }
    }
}
