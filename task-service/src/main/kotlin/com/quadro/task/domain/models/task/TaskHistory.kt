package com.quadro.task.domain.models.task

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class HistoryAction {
    CREATE, UPDATE, STATUS_CHANGE, ASSIGNEE_CHANGE, SPRINT_CHANGE, PRIORITY_CHANGE, TYPE_CHANGE, COMMENT_ADDED,
    ATTACHMENT_ADDED, DELETE
}

data class TaskHistory(
    val id: UUID,
    val taskId: UUID,
    val userId: UUID,
    val action: HistoryAction,
    val oldValue: String?,
    val newValue: String?,
    val createdAt: Instant
)

data class TaskHistoryCreate(
    val action: HistoryAction,
    val oldValue: String? = null,
    val newValue: String? = null
)

@Serializable
data class TaskHistoryResponse(
    val id: String,
    val taskId: String,
    val userId: String,
    val action: HistoryAction,
    val oldValue: String?,
    val newValue: String?,
    val createdAt: Instant
) {
    companion object {
        fun from(history: TaskHistory): TaskHistoryResponse = TaskHistoryResponse(
            id = history.id.toString(),
            taskId = history.taskId.toString(),
            userId = history.userId.toString(),
            action = history.action,
            oldValue = history.oldValue,
            newValue = history.newValue,
            createdAt = history.createdAt
        )
    }
}