package com.quadro.project.domain.models

import java.util.UUID

data class InvitationValidationResult(
    val isValid: Boolean,
    val invitationId: UUID? = null,
    val projectId: UUID? = null,
    val projectName: String? = null,
    val role: ProjectRole? = null,
    val requiresRegistration: Boolean = false,
    val expiresAt: Long? = null,
    val error: String? = null
)
