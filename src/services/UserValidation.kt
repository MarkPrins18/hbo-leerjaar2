package services

import io.ktor.server.plugins.BadRequestException
import requests.RegisterRequest

private const val EMAIL_MAX_LENGTH = 255
private const val PASSWORD_MIN_LENGTH = 8

// BCrypt ignores everything after the first 72 bytes
private const val PASSWORD_MAX_BYTES = 72
private val EMAIL_PATTERN = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

fun RegisterRequest.validate() {
    validateEmail(email.trim())
    validatePassword(password)
}

private fun validateEmail(email: String) {
    if (email.length > EMAIL_MAX_LENGTH || !EMAIL_PATTERN.matches(email)) {
        throw BadRequestException("Veld 'email' bevat geen geldig e-mailadres")
    }
}

private fun validatePassword(password: String) {
    val missingRequirements = buildList {
        if (password.length < PASSWORD_MIN_LENGTH) add("minimaal $PASSWORD_MIN_LENGTH tekens")
        if (password.none(Char::isUpperCase)) add("een hoofdletter")
        if (password.none(Char::isLowerCase)) add("een kleine letter")
        if (password.none(Char::isDigit)) add("een cijfer")
        if (password.all(Char::isLetterOrDigit)) add("een speciaal teken")
    }

    if (missingRequirements.isNotEmpty()) {
        throw BadRequestException("Veld 'password' is te zwak, het mist: ${missingRequirements.joinToString(", ")}")
    }
    if (password.toByteArray().size > PASSWORD_MAX_BYTES) {
        throw BadRequestException("Veld 'password' mag maximaal $PASSWORD_MAX_BYTES bytes lang zijn")
    }
}
