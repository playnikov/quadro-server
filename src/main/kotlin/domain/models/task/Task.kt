package com.quadro.domain.models.task

import java.util.UUID

enum class TaskStatus {
    BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, TESTING, DONE, BLOCKED, CANCELLED
}

enum class TaskPriority {
    CRITICAL, HIGH, MEDIUM, LOW, TRIVIAL
}

enum class TaskType {
    EPIC, STORY, TASK, SUBTASK, BUG, IMPROVEMENT, RESEARCH
}

enum class TaskResolution {
    FIXED, WONT_FIX, DUPLICATE, INVALID, CANT_REPRODUCE, MOVED
}

enum class NotificationLevel {
    ALL,                // Все уведомления
    MENTIONS_ONLY,      // Только упоминания
    NONE                // Без уведомлений
}

data class Task(
    val id: UUID,
    val projectId: UUID,
    val parentId: UUID?,
    val key: String,
    val title: String,
    val description: String?,
    val type: TaskType,
    val status: TaskStatus,
    val priority: TaskPriority,
    val resolution: TaskResolution?,
    val assigneeId: UUID?,
    val reporterId: UUID,
    val storyPoints: Int?,
    val timeEstimate: Long?,
    val timeSpent: Long,
    val dueDate: Long?,
    val startedAt: Long?,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val order: Int,
    val tags: String?
)

data class TaskCreate(
    val projectId: UUID,
    val parentId: UUID? = null,
    val title: String,
    val description: String? = null,
    val type: TaskType = TaskType.TASK,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val assigneeId: UUID? = null,
    val storyPoints: Int? = null,
    val timeEstimate: Long? = null,
    val dueDate: Long? = null,
    val tags: List<String>? = null
)

data class TaskUpdate(
    val title: String? = null,
    val description: String? = null,
    val type: TaskType? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val resolution: TaskResolution? = null,
    val assigneeId: UUID? = null,
    val storyPoints: Int? = null,
    val timeEstimate: Long? = null,
    val timeSpent: Long? = null,
    val dueDate: Long? = null,
    val tags: List<String>? = null
)

data class TaskComment(
    val id: UUID,
    val taskId: UUID,
    val authorId: UUID,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long?
)

data class TaskCommentCreate(
    val content: String
)

data class TaskAttachment(
    val id: UUID,
    val taskId: UUID,
    val uploadedBy: UUID,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val url: String,
    val createdAt: Long
)

data class TaskWatcher(
    val id: UUID,
    val taskId: UUID,
    val userId: UUID,
    val addedAt: Long,
    val notificationLevel: NotificationLevel
)

data class TaskTimeLog(
    val id: UUID,
    val taskId: UUID,
    val userId: UUID,
    val timeSpent: Long,
    val description: String?,
    val loggedAt: Long
)

data class TaskHistory(
    val id: UUID,
    val taskId: UUID,
    val userId: UUID,
    val field: String,
    val oldValue: String?,
    val newValue: String?,
    val createdAt: Long
)

data class TaskListFilters(
    val status: List<TaskStatus>? = null,
    val priority: List<TaskPriority>? = null,
    val type: List<TaskType>? = null,
    val assigneeId: UUID? = null,
    val reporterId: UUID? = null,
    val parentId: UUID? = null,
    val search: String? = null,
    val tags: List<String>? = null,
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val dueFrom: Long? = null,
    val dueTo: Long? = null,
    val page: Int = 1,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortOrder: String = "desc"
)

data class TaskTimeStats(
    val totalTimeSpent: Long,
    val timeEstimate: Long?,
    val remainingTime: Long?,
    val timeSpentToday: Long,
    val timeSpentThisWeek: Long,
    val logsCount: Int
)