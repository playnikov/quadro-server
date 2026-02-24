package com.quadro.presentation.auth

import com.quadro.domain.models.UserCreate
import com.quadro.domain.models.UserLogin
import com.quadro.domain.services.AuthService
import com.quadro.presentation.auth.models.AuthResponse
import com.quadro.presentation.auth.models.ErrorResponse
import com.quadro.presentation.auth.models.LoginRequest
import com.quadro.presentation.auth.models.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import java.util.UUID

class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun register(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()

        try {
            val request = call.receive<RegisterRequest>()

            val userCreate = UserCreate(
                email = request.email,
                username = request.username,
                password = request.password,
                firstName = request.firstName,
                lastName = request.lastName
            )

            val result = authService.register(userCreate)
            result.fold(
                onSuccess = { authResponse ->
                    logger.info("[$requestId] User registered: ${userCreate.email}")
                    call.respond(HttpStatusCode.Created, AuthResponse(token = authResponse.token, refreshToken = authResponse.refreshToken))
                },
                onFailure = { error ->
                    logger.warn("[$requestId] Registration failed: ${error.message}")
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Registration failed"))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Registration error", e)
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format"))
        }
    }

    suspend fun login(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userAgent = call.request.headers["User-Agent"]
        logger.info("[$requestId] Login attempt from: $userAgent")

        try {
            val request = call.receive<LoginRequest>()

            val userLogin = UserLogin(
                email = request.email,
                username = request.username,
                password = request.password
            )

            val result = authService.login(userLogin)

            result.fold(
                onSuccess = { authResponse ->
                    call.respond(HttpStatusCode.OK, AuthResponse(token = authResponse.token, refreshToken = authResponse.refreshToken))
                },
                onFailure = { error ->
                    logger.warn("[$requestId] Login failed: ${error.message}")
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(error.message ?: "Login failed"))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Login error", e)
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format"))
        }
    }
}