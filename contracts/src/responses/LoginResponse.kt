package contracts.responses

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String
)
