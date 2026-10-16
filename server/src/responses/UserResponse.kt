package responses

import kotlinx.serialization.Serializable
import models.User

@Serializable
data class UserResponse(
    val id: Int,
    val email: String
)

fun User.toResponse() = UserResponse(id = id, email = email)
