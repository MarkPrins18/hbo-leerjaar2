package services

import io.ktor.http.HttpStatusCode

/**
 * Base exception for errors that must reach the client with a specific HTTP status.
 * Handled in StatusPages.kt. For 400 and 404, use Ktor's BadRequestException and NotFoundException.
 * For a status without its own class, throw ApiException(HttpStatusCode.X, "message") directly.
 */
open class ApiException(
    val status: HttpStatusCode,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/** 401: the user is not logged in. */
class UnauthorizedException(message: String = "Niet ingelogd") :
    ApiException(HttpStatusCode.Unauthorized, message)

/** 403: the user is logged in but not allowed to do this, e.g. changing someone else's car. */
class ForbiddenException(message: String = "Geen toegang") :
    ApiException(HttpStatusCode.Forbidden, message)

/** 409: the request conflicts with existing data, e.g. a car that is already reserved. */
class ConflictException(message: String) :
    ApiException(HttpStatusCode.Conflict, message)

/** 422: the request is well-formed but cannot be processed, e.g. incomplete RDW data. */
class UnprocessableException(message: String) :
    ApiException(HttpStatusCode.UnprocessableEntity, message)

/** 502: an external service, such as the RDW API, failed or returned an invalid answer. */
class ExternalServiceException(message: String, cause: Throwable? = null) :
    ApiException(HttpStatusCode.BadGateway, message, cause)
