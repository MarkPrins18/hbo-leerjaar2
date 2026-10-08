package routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import requests.LoginRequest
import requests.RegisterRequest
import responses.LoginResponse
import responses.toResponse
import services.AuthService
import services.UserService

fun Route.userRoutes(userService: UserService, authService: AuthService) {
    route("/users") {
        // TODO: temporary endpoint for testing, remove before release
        get {
            val users = userService.getAllUsers().map { it.toResponse() }
            call.respond(HttpStatusCode.OK, users)
        }
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val user = userService.register(request)
            call.respond(HttpStatusCode.Created, user.toResponse())
        }
        post("/login") {
            val request = call.receive<LoginRequest>()
            val token = authService.login(request)
            call.respond(HttpStatusCode.OK, LoginResponse(token))
        }
    }
}
