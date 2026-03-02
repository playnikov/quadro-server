package com.quadro.security

import com.quadro.domain.models.InvitationValidationResult
import java.util.UUID

interface JwtInvitationTokenService {
    fun generateToken(invitationId: UUID, companyId: UUID, teamId: UUID?, expiresInDays: Int? = 7): String
    fun validateToken(token: String): InvitationValidationResult
}