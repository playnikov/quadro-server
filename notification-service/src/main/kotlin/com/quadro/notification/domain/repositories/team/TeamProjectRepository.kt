package com.quadro.notification.domain.repositories.team

import java.util.UUID

interface TeamProjectRepository {
    suspend fun upsert(project: com.quadro.notification.domain.models.team.TeamProject)
    suspend fun delete(teamId: UUID, projectId: UUID)
}