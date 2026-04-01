package com.quadro.shared.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

data class TokenValidationResult(
    val isValid: Boolean,
    val userId: UUID? = null,
    val roles: List<String>? = null,
    val error: String? = null,
    val isExpired: Boolean = false
)

class JwtValidator(
    private val secretKey: String,
    private val issuer: String,
    private val audience: String
) {
    private val algorithm = Algorithm.HMAC256(secretKey)

    fun validateToken(token: String): TokenValidationResult = try {
        val decoded = JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
            .verify(token)
        val userId = runCatching { UUID.fromString(decoded.subject) }.getOrNull()

        val roles = decoded.getClaim("roles").asList(String::class.java)

        TokenValidationResult(
            isValid = true,
            userId = userId,
            roles = roles,
            error = null,
            isExpired = false
        )
    } catch (e: TokenExpiredException) {
        TokenValidationResult(
            isValid = false,
            userId = null,
            roles = null,
            error = "Token expired",
            isExpired = true
        )
    } catch (e: JWTVerificationException) {
        TokenValidationResult(
            isValid = false,
            userId = null,
            roles = null,
            error = "Invalid token",
            isExpired = false
        )
    }
}

fun ApplicationCall.getUserId(): UUID? {
    val principal = principal<UserIdPrincipal>()
    return principal?.name?.let { UUID.fromString(it) }
}