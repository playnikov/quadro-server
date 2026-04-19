package com.quadro.company.domain.services

import com.quadro.company.domain.models.InvitationValidationResult
import java.util.UUID

interface InvitationTokenService {
    fun generateToken(invitationId: UUID, companyId: UUID, expiresInDays: Int? = 7): String
    fun validateToken(token: String): InvitationValidationResult
}