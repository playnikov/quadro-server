package com.quadro.notification.domain.models.team

import java.util.UUID

data class Team(
    val id: UUID,
    val status: TeamStatus
)

enum class TeamStatus {
    ACTIVE, INACTIVE, DELETED
}