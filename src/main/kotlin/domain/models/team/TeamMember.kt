package com.quadro.domain.models.team

import java.util.UUID

data class TeamMember(
    val id: UUID,
    val teamId: UUID,
    val userId: UUID,
    val role: TeamRole,
    val joinedAt: Long,
    val invitedBy: UUID,
    val invitedAt: Long,
    val isActive: Boolean
)

data class TeamMemberResult(
    val id: String,
    val teamId: String,
    val userId: String,
    val userEmail: String,
    val userName: String,
    val userAvatar: String?,
    val role: String,
    val joinedAt: Long,
    val invitedBy: String,
    val invitedByEmail: String,
    val canEdit: Boolean,
    val canRemove: Boolean
)

data class UpdateTeamMemberRole(
    val role: TeamRole
)

data class AddTeamMembersRequest(
    val userIds: List<UUID>,
    val role: TeamRole = TeamRole.MEMBER
)

data class TeamMemberStats(
    val totalMembers: Int,
    val leads: Int,
    val admins: Int,
    val members: Int,
    val guests: Int,
    val activeToday: Int,
    val activeThisWeek: Int
)
