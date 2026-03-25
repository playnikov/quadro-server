package com.quadro.company.domain.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.quadro.company.config.AppConfig
import com.quadro.company.domain.models.InvitationValidationResult
import java.util.Date
import java.util.UUID
import kotlin.time.Clock

class InvitationTokenServiceImpl(
    private val config: AppConfig
) : InvitationTokenService {
    private val algorithm = Algorithm.HMAC256(config.jwt.secret)

    override fun generateToken(
        invitationId: UUID,
        companyId: UUID,
        teamId: UUID?,
        expiresInDays: Int?
    ): String = JWT.create()
        .withIssuer(config.jwt.issuer)
        .withSubject(invitationId.toString())
        .withClaim("invitationId", invitationId.toString())
        .withClaim("companyId", companyId.toString())
        .withClaim("type", "invitation")
        .apply {
            teamId?.let { withClaim("teamId", it.toString()) }
        }
        .withIssuedAt(Date())
        .withExpiresAt(
            Date(
                Clock.System.now().toEpochMilliseconds() +
                        (expiresInDays?.times(24L * 60 * 60 * 1000)
                            ?: config.jwt.invitationExpiration)
            )
        )
        .sign(algorithm)

    override fun validateToken(token: String): InvitationValidationResult = try {
        val verifier = JWT.require(algorithm)
            .withIssuer(config.jwt.issuer)
            .build()
        val decodedJWT = verifier.verify(token)
        val tokenType = decodedJWT.getClaim("type").asString()
        if (tokenType != "invitation") {
            return InvitationValidationResult(
                isValid = false,
                error = "Invalid token type"
            )
        }

        val invitationId = decodedJWT.getClaim("invitationId").asString()
        val companyId = decodedJWT.getClaim("companyId").asString()
        val teamId = decodedJWT.getClaim("teamId").asString()

        InvitationValidationResult(
            isValid = true,
            invitationId = UUID.fromString(invitationId),
            companyId = UUID.fromString(companyId),
            teamId = if (teamId.isNullOrBlank()) null else UUID.fromString(teamId),
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
    }
}