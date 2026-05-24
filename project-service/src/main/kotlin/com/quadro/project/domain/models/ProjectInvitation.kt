package com.quadro.project.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class InviteStatus {
    PENDING, ACCEPTED, EXPIRED, CANCELLED
}

@Serializable
enum class InviteType {
    EMAIL, LINK
}

data class ProjectInvitation(
    val id: UUID,
    val projectId: UUID,
    val invitedBy: UUID,
    val type: InviteType,
    val identifier: String,
    val role: MemberRole,
    val status: InviteStatus,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val acceptedAt: Instant?,
    val acceptedBy: UUID?,
    val message: String?
) {
    fun isExpired(now: Instant) = expiresAt < now
    fun isUsable() = status == InviteStatus.PENDING
}

data class InvitationCreate(
    val role: MemberRole = MemberRole.MEMBER,
    val type: InviteType = InviteType.LINK,
    val identifier: String? = null,
    val message: String? = null,
    val expiresInDays: Int? = null
) {
    fun validate() {
        if (type == InviteType.EMAIL) {
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
    val projectName: String,
    val invitedBy: String,
    val inviteType: InviteType,
    val identifier: String,
    val role: MemberRole,
    val status: InviteStatus,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val acceptedAt: Instant?,
    val acceptedBy: String?,
    val message: String?,
    val link: String?,
) {
    companion object {
        fun from(
            projectName: String,
            invitation: ProjectInvitation,
            link: String
        ): InvitationResponse  = InvitationResponse(
            id = invitation.id.toString(),
            projectName = projectName,
            invitedBy = invitation.invitedBy.toString(),
            inviteType = invitation.type,
            identifier = invitation.identifier,
            role = invitation.role,
            status = invitation.status,
            token = invitation.token,
            expiresAt = invitation.expiresAt,
            createdAt = invitation.createdAt,
            acceptedAt = invitation.acceptedAt,
            acceptedBy = invitation.acceptedBy?.toString(),
            message = invitation.message,
            link = link
        )
    }
}