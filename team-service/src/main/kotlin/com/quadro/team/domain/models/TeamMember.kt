package com.quadro.team.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

data class TeamMember(
    val id: UUID,
    val teamId: UUID,
    val userId: UUID,
    val role: TeamRole,
    val joinedAt: Instant?,
    val invitedAt: Instant,
    val lastActiveAt: Instant?,
    val isActive: Boolean
)

@Serializable
data class TeamMemberResponse(
    val id: String,
    val teamId: String,
    val userId: String,
    val role: TeamRole,
    val joinedAt: Instant?,
    val invitedAt: Instant,
    val lastActiveAt: Instant?,
    val isActive: Boolean
) {
    companion object {
        fun fromTeamMember(teamMember: TeamMember): TeamMemberResponse = TeamMemberResponse(
            id = teamMember.id.toString(),
            teamId = teamMember.teamId.toString(),
            userId = teamMember.userId.toString(),
            role = teamMember.role,
            joinedAt = teamMember.joinedAt,
            invitedAt = teamMember.invitedAt,
            lastActiveAt = teamMember.lastActiveAt,
            isActive = teamMember.isActive
        )
    }
}

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