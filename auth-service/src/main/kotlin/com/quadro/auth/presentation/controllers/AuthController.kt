package com.quadro.auth.presentation.controllers

import com.quadro.auth.domain.models.UserCreate
import com.quadro.auth.domain.models.UserLogin
import com.quadro.auth.domain.services.AuthService
import com.quadro.auth.presentation.models.LoginRequest
import com.quadro.auth.presentation.models.RefreshTokenRequest
import com.quadro.auth.presentation.models.RegisterRequest
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
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
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun login(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userAgent = call.request.headers["User-Agent"]
        val clientIp = call.request.origin.remoteHost
        logger.info("[$requestId] Login attempt from: $clientIp")

        val request = call.receive<LoginRequest>()

        val userLogin = UserLogin(
            name = request.name,
            password = request.password
        )

        val result = authService.login(userLogin, clientIp, userAgent)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun refreshToken(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()
        val result = authService.refreshToken(request.refreshToken)
        call.respond(HttpStatusCode.OK, result)
    }
}