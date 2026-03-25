package com.quadro.company.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class InvitationStatus {
    PENDING, ACCEPTED, EXPIRED, CANCELLED
}

@Serializable
enum class InvitationType {
    EMAIL, LINK
}

data class CompanyInvitation(
    val id: UUID,
    val companyId: UUID,
    val teamId: UUID?,
    val invitedBy: UUID,
    val inviteType: InvitationType,
    val identifier: String,
    val role: CompanyRole,
    val status: InvitationStatus,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val acceptedAt: Instant?,
    val acceptedBy: UUID?,
    val message: String?
)

data class InvitationCreate(
    val teamId: UUID? = null,
    val role: CompanyRole = CompanyRole.MEMBER,
    val inviteType: InvitationType = InvitationType.LINK,
    val identifier: String? = null,
    val message: String? = null,
    val expiresInDays: Int? = null
)

@Serializable
data class InvitationResponse(
    val company: CompanyResponse,
    val teamId: String,
    val invitedBy: String,
    val inviteType: InvitationType,
    val identifier: String,
    val role: CompanyRole,
    val status: InvitationStatus,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val acceptedAt: Instant?,
    val acceptedBy: String,
    val message: String?,
    val link: String?,
) {
    companion object {
        fun fromCompanyInvitation(company: Company, companyInvitation: CompanyInvitation, link: String): InvitationResponse  = InvitationResponse(
            company = CompanyResponse.fromCompany(company),
            teamId = companyInvitation.teamId.toString(),
            invitedBy = companyInvitation.invitedBy.toString(),
            inviteType = companyInvitation.inviteType,
            identifier = companyInvitation.identifier,
            role = companyInvitation.role,
            status = companyInvitation.status,
            token = companyInvitation.token,
            expiresAt = companyInvitation.expiresAt,
            createdAt = companyInvitation.createdAt,
            acceptedAt = companyInvitation.acceptedAt,
            acceptedBy = companyInvitation.acceptedBy.toString(),
            message = companyInvitation.message,
            link = link
        )
    }
}