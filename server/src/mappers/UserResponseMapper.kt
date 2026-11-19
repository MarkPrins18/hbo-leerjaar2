package mappers

import models.User
import shared.responses.UserResponse

fun User.toResponse(bonusPoints: Int) = UserResponse(id = id, email = email, bonusPoints = bonusPoints)
