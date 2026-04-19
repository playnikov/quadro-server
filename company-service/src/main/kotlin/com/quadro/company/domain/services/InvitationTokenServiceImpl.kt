package com.quadro.company.domain.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.quadro.company.domain.models.InvitationValidationResult
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
        companyId: UUID,
        expiresInDays: Int?
    ): String = JWT.create()
        .withIssuer(config.issuer)
        .withSubject(invitationId.toString())
        .withClaim("invitationId", invitationId.toString())
        .withClaim("companyId", companyId.toString())
        .withClaim("type", "invitation")
        .withIssuedAt(Date())
        .withExpiresAt(
            Date(
                Clock.System.now().toEpochMilliseconds() +
                        (expiresInDays?.times(24L * 60 * 60 * 1000)
                            ?: config.invitationExpiration)
            )
        )
        .sign(algorithm)

    override fun validateToken(token: String): InvitationValidationResult = try {
        val verifier = JWT.require(algorithm)
            .withIssuer(config.issuer)
            .withClaim("type", "invitation")
            .build()
        val decodedJWT = verifier.verify(token)

        val invitationId = decodedJWT.getClaim("invitationId").asString()
        val companyId = decodedJWT.getClaim("companyId").asString()
        val teamId = decodedJWT.getClaim("teamId").asString()

        InvitationValidationResult(
            isValid = true,
            invitationId = UUID.fromString(invitationId),
            companyId = UUID.fromString(companyId),
            teamId = teamId?.takeIf { it.isNotBlank() }?.let { UUID.fromString(it) },
            expiresAt = decodedJWT.expiresAt?.time
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