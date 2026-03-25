package com.quadro.auth.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.quadro.auth.config.JwtConfig
import com.quadro.auth.domain.models.User
import com.quadro.shared.security.TokenValidationResult
import java.util.Date
import java.util.UUID
import kotlin.toString

class JwtProvider(private val config: JwtConfig) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    fun generateAccessToken(user: User): String = JWT.create()
        .withSubject(user.id.toString())
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .withClaim("userId", user.id.toString())
        .withClaim("email", user.email)
        .withClaim("username", user.username)
        .withClaim("role", user.role.name)
        .withClaim("tokenType", "access")
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + config.accessExpiration))
        .sign(algorithm)

    fun generateRefreshToken(user: User): String = JWT.create()
        .withSubject(user.id.toString())
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .withClaim("userId", user.id.toString())
        .withClaim("tokenType", "refresh")
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + config.refreshExpiration))
        .sign(algorithm)

    fun generatePasswordResetToken(user: User): String = JWT.create()
        .withSubject(user.id.toString())
        .withIssuer(config.issuer)
        .withClaim("userId", user.id.toString())
        .withClaim("purpose", "password_reset")
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
        .sign(algorithm)
}