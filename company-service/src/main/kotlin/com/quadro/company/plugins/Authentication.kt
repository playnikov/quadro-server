package com.quadro.company.plugins

import com.quadro.shared.security.JwtValidator
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import org.koin.ktor.ext.getKoin
import java.util.UUID
import javax.naming.AuthenticationException

fun Application.configureSecurity() {
    val jwtValidator = getKoin().get<JwtValidator>()

    install(Authentication) {
        bearer("auth-jwt") {
            authenticate { tokenCredential ->
                val token = jwtValidator.validateToken(tokenCredential.token)
                if (token.isValid) {
                    val userId = token.userId
                    UserIdPrincipal(userId.toString())
                } else {
                    throw AuthenticationException("Invalid JWT token")
                }
            }
        }
    }
}