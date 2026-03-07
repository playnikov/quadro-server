package com.quadro.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.quadro.domain.models.company.InvitationValidationResult
import com.typesafe.config.ConfigFactory
import java.util.Date
import java.util.UUID

class JwtInvitationTokenServiceImpl : JwtInvitationTokenService {
    private val config = ConfigFactory.load().getConfig("jwt")

    private val algorithm = Algorithm.HMAC256(config.getString("secret"))

    override fun generateToken(
        invitationId: UUID,
        companyId: UUID,
        teamId: UUID?,
        expiresInDays: Int?
    ): String = JWT.create()
        .withIssuer(config.getString("issuer"))
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
                System.currentTimeMillis() +
                        (expiresInDays?.times(24L * 60 * 60 * 1000)
                            ?: config.getLong("invitationExpiryMs"))
            )
        )
        .sign(algorithm)

    override fun validateToken(token: String): InvitationValidationResult = try {
        val verifier = JWT.require(algorithm)
            .withIssuer(config.getString("issuer"))
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