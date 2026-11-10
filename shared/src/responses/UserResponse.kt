package shared.responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val email: String
)
