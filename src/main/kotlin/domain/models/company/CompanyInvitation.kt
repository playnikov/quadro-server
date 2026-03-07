package com.quadro.domain.models.company

import java.util.UUID

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    EXPIRED,
    CANCELLED
}

data class CompanyInvitation(
    val id: UUID,
    val companyId: UUID,
    val teamId: UUID?,
    val invitedBy: UUID,
    val role: CompanyRole,
    val status: InvitationStatus,
    val token: String,
    val expiresAt: Long,
    val createdAt: Long,
    val acceptedAt: Long?,
    val message: String?,
    val acceptedBy: UUID?
)

data class InvitationCreate(
    val teamId: UUID? = null,
    val role: CompanyRole = CompanyRole.MEMBER,
    val message: String? = null,
    val expiresInDays: Int? = null
)

data class InvitationResult(
    val id: UUID,
    val companyId: UUID,
    val companyName: String,
    val teamId: UUID?,
    val teamName: String?,
    val invitedBy: UUID,
    val invitedByEmail: String,
    val invitedByName: String,
    val role: CompanyRole,
    val status: InvitationStatus,
    val token: String?,
    val expiresAt: Long,
    val createdAt: Long,
    val message: String?,
    val inviteLink: String
)

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