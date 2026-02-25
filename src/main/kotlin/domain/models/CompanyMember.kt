package com.quadro.domain.models

import java.util.UUID

data class CompanyMember(
    val id: UUID,
    val companyId: UUID,
    val userId: UUID,
    val role: CompanyRole,
    val joinedAt: Long,
    val invitedBy: UUID,
    val invitedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean
)

data class CompanyMemberResponse(
    val id: UUID,
    val companyId: UUID,
    val userId: UUID,
    val userEmail: String,
    val userName: String,
    val userAvatar: String?,
    val role: CompanyRole,
    val joinedAt: Long,
    val invitedBy: UUID,
    val invitedByEmail: String,
    val isActive: Boolean
)

data class UpdateMemberRole(
    val role: CompanyRole
)