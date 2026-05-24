package com.quadro.project.domain.models

import java.util.UUID

data class InvitationValidationResult(
    val isValid: Boolean,
    val invitationId: UUID? = null,
    val projectId: UUID? = null,
    val error: String? = null
)
