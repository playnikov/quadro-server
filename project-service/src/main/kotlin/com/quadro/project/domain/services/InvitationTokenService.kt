package com.quadro.project.domain.services

import com.quadro.project.domain.models.InvitationValidationResult
import java.util.UUID

interface InvitationTokenService {
    fun generateToken(invitationId: UUID, projectId: UUID, expiresInDays: Int? = 7): String
    fun validateToken(token: String): InvitationValidationResult
}