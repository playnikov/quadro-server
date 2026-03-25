package com.quadro.company.domain.models

import java.util.UUID

data class InvitationValidationResult(
    val isValid: Boolean,
    val invitationId: UUID? = null,
    val companyId: UUID? = null,
    val companyName: String? = null,
    val teamId: UUID? = null,
    val teamName: String? = null,
    val role: CompanyRole? = null,
    val requiresRegistration: Boolean = false,
    val expiresAt: Long? = null,
    val error: String? = null
)
