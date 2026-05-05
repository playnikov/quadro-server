package com.quadro.task.presentation.models

import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskAttachment
import com.quadro.task.domain.models.task.TaskComment
import com.quadro.task.domain.models.task.TaskPriority
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskType
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
data class TaskResponse(
    val id: String,
    val projectId: String,
    val sprintId: String? = null,
    val parentTaskId: String? = null,
    val number: Int,
    val title: String,
    val description: String? = null,
    val status: TaskStatus,
    val priority: TaskPriority,
    val type: TaskType,
    val assigneeId: String? = null,
    val assignedTeamId: String? = null,
    val reporterId: String,
    val storyPoints: Int? = null,
    val estimatedHours: Double? = null,
    val loggedHours: Double? = null,
    val dueDate: Instant? = null,
    val startedAt: Instant? = null,
    val completedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val labels: List<String>,
    val assignee: String? = null,
    val assignedTeam: String? = null,
    val commentCount: Int = 0,
    val attachmentCount: Int = 0,
    val subtaskCount: Int = 0,
) {
    companion object {
        fun from(task: Task) = TaskResponse(
            id = task.id.toString(),
            projectId = task.projectId.toString(),
            sprintId = task.sprintId?.toString(),
            parentTaskId = task.parentTaskId?.toString(),
            number = task.number,
            title = task.title,
            description = task.description,
            status = task.status,
            priority = task.priority,
            type = task.type,
            assigneeId = task.assigneeId?.toString(),
            assignedTeamId = task.assignedTeamId?.toString(),
            reporterId = task.reporterId.toString(),
            storyPoints = task.storyPoints,
            estimatedHours = task.estimatedHours,
            loggedHours = task.loggedHours,
            dueDate = task.dueDate,
            startedAt = task.startedAt,
            completedAt = task.completedAt,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt,
            labels = task.labels,
        )
    }
}

@Serializable
data class TaskCommentResponse(
    val id: String,
    val taskId: String,
    val authorId: String,
    val content: String,
    val parentCommentId: String?,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val edited: Boolean,
    val mentionedUserIds: List<String>,
) {
    companion object {
        fun from(comment: TaskComment) = TaskCommentResponse(
            id = comment.id.toString(),
            taskId = comment.taskId.toString(),
            authorId = comment.authorId.toString(),
            content = if (comment.isDeleted) "[deleted]" else comment.content,
            parentCommentId = comment.parentId?.toString(),
            isDeleted = comment.isDeleted,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt,
            edited = comment.isEdited,
            mentionedUserIds = comment.mentions.map { it.toString() },
        )
    }
}

@Serializable
data class TaskAttachmentResponse(
    val id: String,
    val taskId: String,
    val uploadedBy: String,
    val fileName: String,
    val fileSize: Long,
    val url: String,
    val createdAt: Instant
) {
    companion object {
        fun from(attachment: TaskAttachment) = TaskAttachmentResponse(
            id = attachment.id.toString(),
            taskId = attachment.taskId.toString(),
            uploadedBy = attachment.uploadedBy.toString(),
            fileName = attachment.fileName,
            fileSize = attachment.fileSize,
            url = attachment.url,
            createdAt = attachment.createdAt,
        )
    }
}