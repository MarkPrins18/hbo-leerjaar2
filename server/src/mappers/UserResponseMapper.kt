package mappers

import models.User
import contracts.responses.UserResponse

fun User.toResponse(bonusPoints: Int) = UserResponse(id = id, email = email, bonusPoints = bonusPoints)
