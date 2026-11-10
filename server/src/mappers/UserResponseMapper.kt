package mappers

import models.User
import shared.responses.UserResponse

fun User.toResponse() = UserResponse(id = id, email = email)
