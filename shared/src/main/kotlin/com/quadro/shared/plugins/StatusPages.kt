package com.quadro.shared.plugins

import com.quadro.shared.dto.DomainException
import com.quadro.shared.dto.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPages")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<DomainException.NotFound> { call, ex ->
            call.respond(HttpStatusCode.NotFound, ApiResponse.error("NOT_FOUND", ex.message ?: "Not found"))
        }
        exception<DomainException.AlreadyExists> { call, ex ->
            call.respond(HttpStatusCode.Conflict, ApiResponse.error("ALREADY_EXISTS", ex.message ?: "Already exists"))
        }
        exception<DomainException.AccessDenied> { call, ex ->
            call.respond(HttpStatusCode.Forbidden, ApiResponse.error("ACCESS_DENIED", ex.message ?: "Forbidden"))
        }
        exception<DomainException.Forbidden> { call, ex ->
            call.respond(HttpStatusCode.Forbidden, ApiResponse.error("FORBIDDEN", ex.message ?: "Forbidden"))
        }
        exception<DomainException.BusinessRule> { call, ex ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiResponse.error("BUSINESS_RULE", ex.message ?: "Business rule violation"))
        }
        exception<DomainException.InvalidTransition> { call, ex ->
            call.respond(HttpStatusCode.UnprocessableEntity, ApiResponse.error("INVALID_TRANSITION", ex.message ?: "Invalid transition"))
        }
        exception<DomainException.ValidationError> { call, ex ->
            call.respond(HttpStatusCode.BadRequest, ApiResponse.error("VALIDATION_ERROR", ex.message ?: "Validation error"))
        }
        exception<IllegalArgumentException> { call, ex ->
            call.respond(HttpStatusCode.BadRequest, ApiResponse.error("BAD_REQUEST", ex.message ?: "Bad request"))
        }
        exception<Throwable> { call, ex ->
            logger.error("Unhandled error in ${call.request.local.uri}", ex)
            call.respond(HttpStatusCode.InternalServerError, ApiResponse.error("INTERNAL_ERROR", "Internal server error"))
        }
    }
}