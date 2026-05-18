package com.quadro.notification.domain.models.team

import java.util.UUID

data class TeamMember(
    val teamId: UUID,
    val userId: UUID,
    val role: TeamRole,
    val isActive: Boolean
)

enum class TeamRole {
    OWNER, ADMIN, MEMBER
}