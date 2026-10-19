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

/** 400: the input is invalid, e.g. a weak password. */
class ValidationException(message: String) :
    ApiException(HttpStatusCode.BadRequest, message)

/** 401: the user is not logged in. */
class UnauthorizedException(message: String = "Niet ingelogd") :
    ApiException(HttpStatusCode.Unauthorized, message)

/** 403: the user is logged in but not allowed to do this, e.g. changing someone else's car. */
class ForbiddenException(message: String = "Geen toegang") :
    ApiException(HttpStatusCode.Forbidden, message)

/** 404: the requested resource does not exist, e.g. an unknown car id. */
class ResourceNotFoundException(message: String) :
    ApiException(HttpStatusCode.NotFound, message)

/** 409: the request conflicts with existing data, e.g. a car that is already reserved. */
class ConflictException(message: String) :
    ApiException(HttpStatusCode.Conflict, message)

/** 413: an uploaded file is too large, e.g. a photo over the size limit. */
class FileTooLargeException(message: String) :
    ApiException(HttpStatusCode.PayloadTooLarge, message)

/** 415: an uploaded file has an unsupported type, e.g. a PNG where only JPEG is allowed. */
class UnsupportedFileTypeException(message: String) :
    ApiException(HttpStatusCode.UnsupportedMediaType, message)

/** 422: the request is well-formed but cannot be processed, e.g. incomplete RDW data. */
class UnprocessableException(message: String) :
    ApiException(HttpStatusCode.UnprocessableEntity, message)

/** 502: an external service, such as the RDW API, failed or returned an invalid answer. */
class ExternalServiceException(message: String, cause: Throwable? = null) :
    ApiException(HttpStatusCode.BadGateway, message, cause)
