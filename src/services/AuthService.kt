package services

import org.mindrot.jbcrypt.BCrypt
import repositories.UserRepository
import requests.LoginRequest

class AuthService(
    private val userRepository: UserRepository,
    private val tokenService: TokenService
) {
    suspend fun login(request: LoginRequest): String {
        val user = userRepository.findByEmail(request.email.trim().lowercase())
            ?: throw invalidCredentials()

        if (!BCrypt.checkpw(request.password, user.passwordHash)) throw invalidCredentials()

        return tokenService.createToken(user)
    }

    private fun invalidCredentials() = UnauthorizedException("Onjuist e-mailadres of wachtwoord")
}
