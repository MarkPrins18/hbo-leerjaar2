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
            ?: invalidCredentials()

        if (!BCrypt.checkpw(request.password, user.passwordHash)) invalidCredentials()

        return tokenService.createToken(user)
    }

    private fun invalidCredentials(): Nothing = throw UnauthorizedException("Onjuist e-mailadres of wachtwoord")
}
