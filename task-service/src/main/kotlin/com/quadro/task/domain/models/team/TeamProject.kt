package com.quadro.task.domain.models.team

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class TeamProjectRole {
    VIEWER,       // только просмотр задач
    CONTRIBUTOR,  // может брать задачи
    ASSIGNEE,     // задачи назначаются команде
    MANAGER       // управляет задачами в проекте
}

data class TeamProject(
    val teamId: UUID,
    val projectId: UUID,
    val role: TeamProjectRole
)