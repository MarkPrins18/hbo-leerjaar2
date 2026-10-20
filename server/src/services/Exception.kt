package services

import io.ktor.http.HttpStatusCode

/**
 * Base exception for errors that must reach the client with a specific HTTP status.
 * Handled in StatusPages.kt. Always throw one of the classes below, not Ktor's own exceptions.
 * For a status without its own class, throw ApiException(HttpStatusCode.X, "message") directly.
 */
open class ApiException(
    val status: HttpStatusCode,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class ValidationException(message: String) :
    ApiException(HttpStatusCode.BadRequest, message)

class UnauthorizedException(message: String = "Niet ingelogd") :
    ApiException(HttpStatusCode.Unauthorized, message)

class ForbiddenException(message: String = "Geen toegang") :
    ApiException(HttpStatusCode.Forbidden, message)

class ResourceNotFoundException(message: String) :
    ApiException(HttpStatusCode.NotFound, message) {
    constructor(resource: String, id: Int) : this("$resource $id niet gevonden")
}

class ConflictException(message: String) :
    ApiException(HttpStatusCode.Conflict, message)

class FileTooLargeException(message: String) :
    ApiException(HttpStatusCode.PayloadTooLarge, message)

class UnsupportedFileTypeException(message: String) :
    ApiException(HttpStatusCode.UnsupportedMediaType, message)

class UnprocessableException(message: String) :
    ApiException(HttpStatusCode.UnprocessableEntity, message)

class ExternalServiceException(message: String, cause: Throwable? = null) :
    ApiException(HttpStatusCode.BadGateway, message, cause)
