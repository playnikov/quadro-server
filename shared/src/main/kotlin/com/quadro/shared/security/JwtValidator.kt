package com.quadro.shared.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.quadro.shared.data.config.JwtConfig
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.bearer
import io.ktor.server.auth.principal
import io.ktor.server.request.httpMethod
import org.slf4j.LoggerFactory
import java.util.UUID
import javax.naming.AuthenticationException

class UserPrincipal(val userId: UUID, val role: String) : Principal

data class TokenValidationResult(
    val isValid: Boolean,
    val userId: UUID? = null,
    val role: String? = null,
    val error: String? = null,
    val isExpired: Boolean = false
)

class JwtValidator(
    private val config: JwtConfig
) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    fun validateToken(token: String): TokenValidationResult = try {
        val decoded = JWT.require(algorithm)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .build()
            .verify(token)
        val userId = runCatching { UUID.fromString(decoded.subject) }.getOrNull()

        val role = decoded.getClaim("role").toString()

        TokenValidationResult(
            isValid = true,
            userId = userId,
            role = role,
            error = null,
            isExpired = false
        )
    } catch (e: TokenExpiredException) {
        TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = e.message ?: "Token expired",
            isExpired = true
        )
    } catch (e: JWTVerificationException) {
        TokenValidationResult(
            isValid = false,
            userId = null,
            role = null,
            error = e.message ?: "Invalid token",
            isExpired = false
        )
    }
}

fun Application.configureSecurity(jwtValidator: JwtValidator) {
    install(Authentication) {
        bearer("auth-jwt") {
            skipWhen { it.request.httpMethod == HttpMethod.Options }
            authenticate { tokenCredential ->
                val token = jwtValidator.validateToken(tokenCredential.token)
                if (token.isValid && token.userId != null) {
                    UserPrincipal(token.userId, token.role ?: "USER")
                } else {
                    null
                }
            }
        }
    }
}

fun ApplicationCall.getUserId(): UUID? = principal<UserPrincipal>()?.userId
fun ApplicationCall.getRole(): String? = principal<UserPrincipal>()?.role