package com.quadro.task.domain.models.team

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class TeamStatus {
    ACTIVE, ARCHIVED, DISBAND
}

data class Team(
    val id: UUID,
    val status: TeamStatus
)