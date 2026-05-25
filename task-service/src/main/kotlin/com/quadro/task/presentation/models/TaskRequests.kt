package com.quadro.task.presentation.models

import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskType
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class TaskCreateRequest(
    val title: String,
    val projectId: String,
    val description: String? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val type: TaskType = TaskType.TASK,
    val assigneeId: String? = null,
    val assignedTeamId: String? = null,
    val sprintId: String? = null,
    val parentTaskId: String? = null,
    val storyPoints: Int? = null,
    val estimatedHours: Double? = null,
    val dueDate: Instant? = null,
    val labels: List<String> = emptyList(),
)

@Serializable
data class TaskUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val assigneeId: String? = null,
    val assignedTeamId: String? = null,
    val sprintId: String? = null,
    val storyPoints: Int? = null,
    val estimatedHours: Double? = null,
    val loggedHours: Double? = null,
    val dueDate: Instant? = null,
    val labels: List<String>? = null,
)

@Serializable
data class TaskCommentCreateRequest(
    val taskId: String,
    val content: String,
    val parentId: String? = null,
    val mentions: List<String>? = null
)

@Serializable
data class TaskCommentUpdateRequest(
    val content: String,
)

@Serializable
data class TaskAttachmentCreateRequest(
    val fileName: String,
    val fileSize: Long,
    val url: String,
)