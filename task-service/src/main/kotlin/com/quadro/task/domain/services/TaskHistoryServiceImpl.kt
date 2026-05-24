package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.HistoryAction
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskHistory
import com.quadro.task.domain.repositories.task.TaskHistoryRepository
import java.util.UUID
import kotlin.time.Clock

class TaskHistoryServiceImpl(
    private val historyRepository: TaskHistoryRepository
) : TaskHistoryService {
    override suspend fun recordTaskCreate(task: Task, userId: UUID) {
        createHistory(task.id, userId, HistoryAction.CREATE, null, task.title)
    }

    override suspend fun recordStatusChange(taskId: UUID, userId: UUID, oldStatus: String, newStatus: String) {
        createHistory(taskId, userId, HistoryAction.STATUS_CHANGE, oldStatus, newStatus)
    }

    override suspend fun recordAssigneeChange(taskId: UUID, userId: UUID, oldAssignee: UUID?, newAssignee: UUID?) {
        createHistory(taskId, userId, HistoryAction.ASSIGNEE_CHANGE, oldAssignee?.toString(), newAssignee?.toString())
    }

    override suspend fun recordSprintChange(taskId: UUID, userId: UUID, oldSprint: UUID?, newSprint: UUID?) {
        createHistory(taskId, userId, HistoryAction.SPRINT_CHANGE, oldSprint?.toString(), newSprint?.toString())
    }

    override suspend fun recordPriorityChange(taskId: UUID, userId: UUID, oldPriority: String, newPriority: String) {
        createHistory(taskId, userId, HistoryAction.PRIORITY_CHANGE, oldPriority, newPriority)
    }

    override suspend fun recordCommentAdded(taskId: UUID, userId: UUID, commentId: UUID) {
        createHistory(taskId, userId, HistoryAction.COMMENT_ADDED, null, commentId.toString())
    }

    private suspend fun createHistory(taskId: UUID, userId: UUID, action: HistoryAction, oldValue: String?, newValue: String?) {
        val history = TaskHistory(
            id = UUID.randomUUID(),
            taskId = taskId,
            userId = userId,
            action = action,
            oldValue = oldValue,
            newValue = newValue,
            createdAt = Clock.System.now()
        )
        historyRepository.create(history)
    }
}