package com.quadro.domain.models

import java.util.UUID

data class CompanyInvitation(
    val id: UUID,
    val companyId: UUID,
    val invitedBy: UUID,
    val identifier: String,
    val role: CompanyRole,
    val status: InvitationStatus,
    val token: String?,
    val expiresAt: Long,
    val createdAt: Long,
    val acceptedAt: Long?,
    val message: String?
)

data class InvitationCreate(
    val identifier: String,
    val role: CompanyRole = CompanyRole.MEMBER,
    val message: String? = null,
    val expiresInDays: Int? = null
)

data class InvitationResult(
    val id: UUID,
    val companyId: UUID,
    val companyName: String,
    val invitedBy: UUID,
    val invitedByEmail: String,
    val identifier: String,
    val role: CompanyRole,
    val status: InvitationStatus,
    val token: String?,
    val expiresAt: Long,
    val createdAt: Long,
    val message: String?,
    val inviteLink: String?
)

data class AcceptInvitation(
    val token: String? = null,
    val identifier: String
)