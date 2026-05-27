package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.Task
import java.util.UUID

interface TaskAssignmentService {
    suspend fun assignTaskToUser(taskId: UUID, userId: UUID, requestId: UUID): Task
    suspend fun unassignTask(taskId: UUID, requestId: UUID): Task
}