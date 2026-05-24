package com.quadro.project.domain.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.quadro.project.domain.models.InvitationValidationResult
import com.quadro.shared.data.config.JwtConfig
import java.util.Date
import java.util.UUID
import kotlin.time.Clock

class InvitationTokenServiceImpl(
    private val config: JwtConfig
) : InvitationTokenService {
    private val algorithm = Algorithm.HMAC256(config.secret)

    override fun generateToken(
        invitationId: UUID,
        projectId: UUID,
    ): String = JWT.create()
        .withIssuer(config.issuer)
        .withSubject(invitationId.toString())
        .withClaim("projectId", projectId.toString())
        .withIssuedAt(Date())
        .sign(algorithm)

    override fun validateToken(token: String): InvitationValidationResult = try {
        val verifier = JWT.require(algorithm)
            .withIssuer(config.issuer)
            .build()
        val decoded = verifier.verify(token)

        val invitationId = decoded.subject
        val projectId = decoded.getClaim("projectId").asString()

        if (invitationId == null || projectId == null) {
            return InvitationValidationResult(
                isValid = false,
                error = "Invalid token claims"
            )
        }

        InvitationValidationResult(
            isValid = true,
            invitationId = UUID.fromString(invitationId),
            projectId = UUID.fromString(projectId)
        )
    } catch (e: TokenExpiredException) {
        InvitationValidationResult(
            isValid = false,
            error = "Invitation expired"
        )
    } catch (e: JWTVerificationException) {
        InvitationValidationResult(
            isValid = false,
            error = "Invalid invitation token"
        )
    } catch (e: IllegalArgumentException) {
        InvitationValidationResult(
            isValid = false,
            error = "Malformed token claims"
        )
    }
}