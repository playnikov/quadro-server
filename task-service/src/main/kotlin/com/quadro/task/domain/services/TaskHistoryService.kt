package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.HistoryAction
import com.quadro.task.domain.models.task.Task
import java.util.UUID

interface TaskHistoryService {
    suspend fun recordTaskCreate(task: Task, userId: UUID)
    suspend fun recordStatusChange(taskId: UUID, userId: UUID, oldStatus: String, newStatus: String)
    suspend fun recordAssigneeChange(taskId: UUID, userId: UUID, oldAssignee: UUID?, newAssignee: UUID?)
    suspend fun recordSprintChange(taskId: UUID, userId: UUID, oldSprint: UUID?, newSprint: UUID?)
    suspend fun recordPriorityChange(taskId: UUID, userId: UUID, oldPriority: String, newPriority: String)
    suspend fun recordCommentAdded(taskId: UUID, userId: UUID, commentId: UUID)
}