package com.quadro.task.domain.models.team

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class TeamRole {
    MEMBER,     // Может просматривать задачи и комментировать
    LEAD,       // Может назначать задачи и управлять спринтами
    MANAGER     // Полный контроль над командой и проектами
}

data class TeamMember(
    val teamId: UUID,
    val userId: UUID,
    val role: TeamRole,
    val isActive: Boolean
)