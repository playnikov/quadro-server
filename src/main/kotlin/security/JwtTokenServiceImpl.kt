package com.quadro.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.quadro.domain.models.user.TokenValidationResult
import com.quadro.domain.models.user.User
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.UUID

class JwtTokenServiceImpl : JwtTokenService {
    private val config = ConfigFactory.load().getConfig("jwt")

    private val algorithm = Algorithm.HMAC256(config.getString("secret"))
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun generateAccessToken(user: User): String = JWT.create()
        .withSubject(user.id.toString())
        .withIssuer(config.getString("issuer"))
        .withAudience(config.getString("audience"))
        .withClaim("userId", user.id.toString())
        .withClaim("email", user.email)
        .withClaim("username", user.username)
        .withClaim("role", user.role.name)
        .withClaim("tokenType", "access")
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + config.getLong("accessExpiration")))
        .sign(algorithm)

    override fun generateRefreshToken(user: User): String = JWT.create()
        .withSubject(user.id.toString())
        .withIssuer(config.getString("issuer"))
        .withAudience(config.getString("audience"))
        .withClaim("userId", user.id.toString())
        .withClaim("tokenType", "refresh")
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + config.getLong("refreshExpiration")))
        .sign(algorithm)

    override fun validateToken(token: String): TokenValidationResult = try {
        val verifier = JWT.require(algorithm)
            .withIssuer(config.getString("issuer"))
            .build()

        val decodedJWT = verifier.verify(token)
        TokenValidationResult(
            userId = UUID.fromString(decodedJWT.subject),
            isValid = true
        )
    } catch (e: TokenExpiredException) {
        TokenValidationResult(userId = null, isValid = false, isExpired = true, error = "Token expired")
    } catch (e: JWTVerificationException) {
        TokenValidationResult(userId = null, isValid = false, error = "Invalid token")
    }
}