package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import java.util.UUID

interface TaskStatusService {
    suspend fun transitionStatus(taskId: UUID, newStatus: TaskStatus): Task
    suspend fun validateStatusTransition(taskId: UUID, newStatus: TaskStatus): Boolean
    suspend fun startTask(taskId: UUID): Task
    suspend fun completeTask(taskId: UUID): Task
    suspend fun cancelTask(taskId: UUID): Task
    suspend fun reopenTask(taskId: UUID): Task
}