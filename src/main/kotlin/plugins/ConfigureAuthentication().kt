package com.quadro.plugins

import com.quadro.security.JwtTokenService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureAuthentication() {
    val jwtTokenService: JwtTokenService by inject()

    install(Authentication) {
        bearer("auth-jwt") {
            authenticate { tokenCredential ->
                val token = jwtTokenService.validateToken(tokenCredential.token)
                if (token.isValid) {
                    val userId = token.userId
                    UserIdPrincipal(userId.toString())
                } else {
                    throw AuthenticationException()
                }
            }
        }
    }
}

fun ApplicationCall.getUserId(): UUID? {
    return principal<UserIdPrincipal>()?.name?.let { UUID.fromString(it) }
}