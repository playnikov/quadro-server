package com.quadro.project.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class MemberRole {
    GUEST, MEMBER, MANAGER, OWNER;

    fun isAtLeast(other: MemberRole) = ordinal >= other.ordinal
    fun isHigherThan(other: MemberRole) = ordinal > other.ordinal
}

data class ProjectMember(
    val id: UUID,
    val projectId: UUID,
    val userId: UUID,
    val role: MemberRole,
    val joinedAt: Instant,
    val invitedBy: UUID,
    val invitedAt: Instant
)

@Serializable
data class ProjectMemberResponse(
    val id: String,
    val projectId: String,
    val userId: String,
    val role: MemberRole,
    val joinedAt: Instant,
    val invitedBy: String,
    val invitedAt: Instant,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val email: String,
) {
    companion object {
        fun from(member: ProjectMember, user: User): ProjectMemberResponse = ProjectMemberResponse(
            id = member.id.toString(),
            projectId = member.projectId.toString(),
            userId = member.userId.toString(),
            role = member.role,
            joinedAt = member.joinedAt,
            invitedBy = member.invitedBy.toString(),
            invitedAt = member.invitedAt,
            firstName = user.firstName,
            lastName = user.lastName,
            middleName = user.middleName,
            email = user.email
        )
    }
}