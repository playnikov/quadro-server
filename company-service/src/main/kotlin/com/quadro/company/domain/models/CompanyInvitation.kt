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
) {
    fun isExpired(now: Instant) = expiresAt < now
    fun isUsable() = status == InvitationStatus.PENDING
}

data class InvitationCreate(
    val teamId: UUID? = null,
    val role: CompanyRole = CompanyRole.MEMBER,
    val inviteType: InvitationType = InvitationType.LINK,
    val identifier: String? = null,
    val message: String? = null,
    val expiresInDays: Int? = null
) {
    fun validate() {
        if (inviteType == InvitationType.EMAIL) {
            requireNotNull(identifier) { "Email is required for EMAIL invite type" }
            require(identifier.contains("@")) { "Invalid email format" }
        }
        expiresInDays?.let {
            require(it in 1..30) { "Expiry must be between 1 and 30 days" }
        }
    }
}

@Serializable
data class InvitationResponse(
    val id: String,
    val company: CompanyResponse,
    val teamId: String?,
    val invitedBy: String,
    val inviteType: InvitationType,
    val identifier: String,
    val role: CompanyRole,
    val status: InvitationStatus,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val acceptedAt: Instant?,
    val acceptedBy: String?,
    val message: String?,
    val link: String?,
) {
    companion object {
        fun fromCompanyInvitation(
            company: Company,
            invitation: CompanyInvitation,
            link: String,
            owner: User? = null,
        ): InvitationResponse  = InvitationResponse(
            id = invitation.id.toString(),
            company = CompanyResponse.fromCompany(company, owner),
            teamId = invitation.teamId?.toString(),
            invitedBy = invitation.invitedBy.toString(),
            inviteType = invitation.inviteType,
            identifier = invitation.identifier,
            role = invitation.role,
            status = invitation.status,
            token = invitation.token,
            expiresAt = invitation.expiresAt,
            createdAt = invitation.createdAt,
            acceptedAt = invitation.acceptedAt,
            acceptedBy = invitation.acceptedBy?.toString(),
            message = invitation.message,
            link = link,
        )
    }
}