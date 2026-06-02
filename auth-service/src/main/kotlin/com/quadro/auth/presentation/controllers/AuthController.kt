package com.quadro.auth.presentation.controllers

import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.services.AuthService
import com.quadro.auth.presentation.models.ChangePasswordRequest
import com.quadro.auth.presentation.models.LoginRequest
import com.quadro.auth.presentation.models.RefreshTokenRequest
import com.quadro.auth.presentation.models.RegisterRequest
import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.collections.mapOf
import kotlin.time.Duration.Companion.days

class AuthController(private val authService: AuthService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun register(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val clientIp = call.request.origin.remoteHost
        logger.info("[$requestId] Registration attempt from: $clientIp")

        val request = try {
            call.receive<RegisterRequest>()
        } catch (e: Exception) {
            throw DomainException.ValidationError("Invalid request body: ${e.message}")
        }
        val result = authService.register(
            UserCreate(
                username = request.username,
                email = request.email,
                password = request.password,
                lastName = request.lastName,
                firstName = request.firstName,
                isNeedChangePassword = false,
                middleName = request.middleName
            ),
            clientIp
        )
        call.response.cookies.append(
            Cookie(
                name = "refresh_token",
                value = result.second,
                httpOnly = true,
                secure = true,
                path = "/api/auth/refresh",
                maxAge = 30.days.inWholeSeconds.toInt()
            )
        )
        call.respond(HttpStatusCode.Created, ApiResponse.ok(mapOf("token" to result.first)))
    }

    suspend fun login(call: ApplicationCall) {
        val userAgent = call.request.headers["User-Agent"]

        val request = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            throw DomainException.ValidationError("Invalid request body: ${e.message}")
        }

        val userLogin = UserLogin(
            name = request.name,
            password = request.password
        )

        val result = authService.login(userLogin, userAgent)
        call.response.cookies.append(
            Cookie(
                name = "refresh_token",
                value = result.second,
                httpOnly = true,
                secure = true,
                path = "/api/auth/refresh",
                maxAge = 30.days.inWholeSeconds.toInt()
            )
        )
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("token" to result.first)))
    }

    suspend fun refreshToken(call: ApplicationCall) {
        val refreshToken = call.request.cookies["refresh_token"]
            ?: throw DomainException.ValidationError("Refresh token is invalid")
        val result = authService.refreshToken(refreshToken)
        call.response.cookies.append(
            Cookie(
                name = "refresh_token",
                value = result.second,
                httpOnly = true,
                secure = true,
                path = "/api/auth/refresh",
                maxAge = 30.days.inWholeSeconds.toInt()
            )
        )
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("token" to result.first)))
    }

    suspend fun changePassword(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val request = try {
            call.receive<ChangePasswordRequest>()
        } catch (e: Exception) {
            throw DomainException.ValidationError("Invalid request body: ${e.message}")
        }

        if (request.currentPassword == null) {
            authService.changePassword(userId, request.newPassword)
        } else {
            authService.changePassword(userId, request.currentPassword, request.newPassword)
        }
        call.respond(HttpStatusCode.OK, ApiResponse.ok("Password changed"))
    }
}