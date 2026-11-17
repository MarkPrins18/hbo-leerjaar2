package services

import mappers.toResponse
import models.User
import org.mindrot.jbcrypt.BCrypt
import repositories.RideRepository
import repositories.UserRepository
import shared.requests.RegisterRequest
import shared.responses.UserResponse

class UserService(
    private val userRepository: UserRepository,
    private val rideRepository: RideRepository
) {

    /** A new account has no rides yet, so its bonus points balance is 0. */
    suspend fun register(request: RegisterRequest): UserResponse {
        request.validate()
        val email = request.email.trim().lowercase()

        if (userRepository.findByEmail(email) != null) {
            throw ConflictException("Er bestaat al een account met dit e-mailadres")
        }

        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        return userRepository.create(email, passwordHash).toResponse(bonusPoints = 0)
    }

    suspend fun getUserResponse(userId: Int): UserResponse {
        val user = userRepository.findById(userId) ?: throw ResourceNotFoundException("Gebruiker", userId)
        return user.withBonusPoints()
    }

    suspend fun getAllUserResponses(): List<UserResponse> = userRepository.findAll().map { it.withBonusPoints() }

    private suspend fun User.withBonusPoints(): UserResponse =
        toResponse(rideRepository.getTotalBonusPointsByUserId(id))
}
