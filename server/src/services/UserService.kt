package services

import models.User
import org.mindrot.jbcrypt.BCrypt
import repositories.UserRepository
import requests.RegisterRequest

class UserService(private val userRepository: UserRepository) {

    suspend fun register(request: RegisterRequest): User {
        request.validate()
        val email = request.email.trim().lowercase()

        if (userRepository.findByEmail(email) != null) {
            throw ConflictException("Er bestaat al een account met dit e-mailadres")
        }

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        return userRepository.create(email, passwordHash)
    }

    suspend fun getAllUsers(): List<User> = userRepository.findAll()
}
