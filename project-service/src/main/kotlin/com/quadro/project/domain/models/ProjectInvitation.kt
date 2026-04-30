package com.quadro.project.domain.models

import com.quadro.project.presentation.models.ProjectResponse
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

data class ProjectInvitation(
    val id: UUID,
    val projectId: UUID,
    val invitedBy: UUID,
    val inviteType: InvitationType,
    val identifier: String,
    val role: ProjectRole,
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
    val role: ProjectRole = ProjectRole.MEMBER,
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
    val project: ProjectResponse,
    val invitedBy: String,
    val inviteType: InvitationType,
    val identifier: String,
    val role: ProjectRole,
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
        fun from(
            project: Project,
            invitation: ProjectInvitation,
            link: String
        ): InvitationResponse  = InvitationResponse(
            id = invitation.id.toString(),
            project = ProjectResponse.from(project),
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
            link = link
        )
    }
}