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
    val invitedAt: Instant?,
    val invitedBy: UUID,
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
    val invitedAt: Instant?,
    val invitedBy: String,
    val lastActiveAt: Instant?,
    val isActive: Boolean
) {
    companion object {
        fun from(teamMember: TeamMember): TeamMemberResponse = TeamMemberResponse(
            id = teamMember.id.toString(),
            teamId = teamMember.teamId.toString(),
            userId = teamMember.userId.toString(),
            role = teamMember.role,
            joinedAt = teamMember.joinedAt,
            invitedBy = teamMember.invitedBy.toString(),
            invitedAt = teamMember.invitedAt,
            lastActiveAt = teamMember.lastActiveAt,
            isActive = teamMember.isActive
        )
    }
}

data class UpdateTeamMemberRole(
    val role: TeamRole
)