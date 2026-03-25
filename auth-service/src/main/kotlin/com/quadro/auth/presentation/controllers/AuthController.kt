package com.quadro.auth.presentation.controllers

import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.services.AuthService
import com.quadro.auth.presentation.models.LoginRequest
import com.quadro.auth.presentation.models.RefreshTokenRequest
import com.quadro.auth.presentation.models.RegisterRequest
import com.quadro.shared.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import java.util.UUID

class AuthController(private val authService: AuthService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun register(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val clientIp = call.request.origin.remoteHost
        logger.info("[$requestId] Registration attempt from: $clientIp")

        try {
            val request = call.receive<RegisterRequest>()
            val result = authService.register(
                UserCreate(
                    username = request.username,
                    email = request.email,
                    password = request.password,
                    lastName = request.lastName,
                    firstName = request.firstName,
                    middleName = request.middleName
                ),
                clientIp
            )
            result.fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { call.respond(HttpStatusCode.BadRequest, ErrorResponse(it.message ?: "Registration failed")) }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Registration error", e)
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format"))
        }
    }

    suspend fun login(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userAgent = call.request.headers["User-Agent"]
        val clientIp = call.request.origin.remoteHost
        logger.info("[$requestId] Login attempt from: $clientIp")

        try {
            val request = call.receive<LoginRequest>()

            val userLogin = UserLogin(
                email = request.email,
                username = request.username,
                password = request.password
            )

            val result = authService.login(userLogin, clientIp, userAgent)
            result.fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { call.respond(HttpStatusCode.Unauthorized, ErrorResponse(it.message ?: "Login failed")) }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Login error", e)
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format"))
        }
    }

    suspend fun refreshToken(call: ApplicationCall) {
        return try {
            val request = call.receive<RefreshTokenRequest>()
            val result = authService.refreshToken(request.refreshToken)

            result.fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { call.respond(HttpStatusCode.Unauthorized, ErrorResponse(it.message ?: "Token refresh failed")) }
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request"))
        }
    }
}