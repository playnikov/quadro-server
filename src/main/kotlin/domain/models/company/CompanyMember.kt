package com.quadro.domain.models.company

import java.util.UUID

data class CompanyMember(
    val id: UUID,
    val companyId: UUID,
    val userId: UUID,
    val role: CompanyRole,
    val joinedAt: Long,
    val invitedBy: UUID,
    val invitedAt: Long,
    val isActive: Boolean
)

data class CompanyMemberResult(
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

data class UpdateCompanyMemberRole(
    val role: CompanyRole
)