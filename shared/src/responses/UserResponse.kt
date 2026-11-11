package shared.responses

import kotlinx.serialization.Serializable

/** [bonusPoints] is the balance: the sum of the bonus points of all completed rides. */
@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val bonusPoints: Int
)
