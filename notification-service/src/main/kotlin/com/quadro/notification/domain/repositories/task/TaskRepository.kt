package com.quadro.notification.domain.repositories.task

import java.util.UUID

interface TaskRepository {
    suspend fun clearAssignee(userId: UUID)
    suspend fun clearAssignedTeam(teamId: UUID)
}