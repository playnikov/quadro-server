package com.quadro.project.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class ProjectRole {
    GUEST, MEMBER, MANAGER, ADMIN, OWNER;

    fun isAtLeast(other: ProjectRole) = ordinal >= other.ordinal
    fun isHigherThan(other: ProjectRole) = ordinal > other.ordinal
}

data class ProjectMember(
    val id: UUID,
    val projectId: UUID,
    val userId: UUID,
    val role: ProjectRole,
    val joinedAt: Instant,
    val invitedBy: UUID,
    val invitedAt: Instant
)

@Serializable
data class ProjectMemberResponse(
    val id: String,
    val projectId: String,
    val userId: String,
    val role: ProjectRole,
    val joinedAt: Instant,
    val invitedBy: String,
    val invitedAt: Instant
) {
    companion object {
        fun from(member: ProjectMember): ProjectMemberResponse = ProjectMemberResponse(
            id = member.id.toString(),
            projectId = member.projectId.toString(),
            userId = member.userId.toString(),
            role = member.role,
            joinedAt = member.joinedAt,
            invitedBy = member.invitedBy.toString(),
            invitedAt = member.invitedAt
        )
    }
}