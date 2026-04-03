package com.quadro.team.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

data class TeamProject(
    val id: UUID,
    val teamId: UUID,
    val projectId: UUID,
    val assignedAt: Instant,
    val assignedBy: UUID,
    val isActive: Boolean
)

@Serializable
data class TeamProjectResponse(
    val id: String,
    val teamId: String,
    val projectId: String,
    val assignedAt: Instant,
    val assignedBy: String,
    val isActive: Boolean
) {
    companion object {
        fun fromTeam(team: TeamProject): TeamProjectResponse = TeamProjectResponse(
            id = team.id.toString(),
            teamId = team.teamId.toString(),
            projectId = team.projectId.toString(),
            assignedAt = team.assignedAt,
            assignedBy = team.assignedBy.toString(),
            isActive = team.isActive
        )
    }
}