package com.quadro.company.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

data class CompanyMember(
    val id: UUID,
    val companyId: UUID,
    val userId: UUID,
    val role: CompanyRole,
    val joinedAt: Instant,
    val invitedBy: UUID,
    val invitedAt: Instant,
    val lastActiveAt: Instant?,
    val isActive: Boolean
)

@Serializable
data class CompanyMemberResponse(
    val id: String,
    val companyId: String,
    val userId: String,
    val role: String,
    val joinedAt: Instant,
    val invitedBy: String,
    val invitedAt: Instant,
    val lastActiveAt: Instant?,
    val isActive: Boolean
) {
    companion object {
        fun fromCompanyMember(companyMember: CompanyMember): CompanyMemberResponse = CompanyMemberResponse(
            id = companyMember.id.toString(),
            companyId = companyMember.companyId.toString(),
            userId = companyMember.userId.toString(),
            role = companyMember.role.name,
            joinedAt = companyMember.joinedAt,
            invitedBy = companyMember.invitedBy.toString(),
            invitedAt = companyMember.invitedAt,
            lastActiveAt = companyMember.lastActiveAt,
            isActive = companyMember.isActive
        )
    }
}