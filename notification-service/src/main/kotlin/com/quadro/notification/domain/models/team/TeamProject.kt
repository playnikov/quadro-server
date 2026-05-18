package com.quadro.notification.domain.models.team

import java.util.UUID

data class TeamProject(
    val teamId: UUID,
    val projectId: UUID,
    val role: TeamProjectRole
)

enum class TeamProjectRole {
    OWNER, ADMIN, MEMBER
}