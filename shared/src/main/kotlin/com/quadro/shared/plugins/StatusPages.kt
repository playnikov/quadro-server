package com.quadro.shared.plugins

import com.quadro.shared.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import javax.security.sasl.AuthenticationException
import kotlin.time.Clock

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = cause.message ?: "Internal server error",
                    code = "INTERNAL_ERROR",
                    timestamp = Clock.System.now()
                )
            )
        }
        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(
                    message = "Invalid or missing authentication token",
                    code = "UNAUTHORIZED",
                )
            )
        }
    }
}