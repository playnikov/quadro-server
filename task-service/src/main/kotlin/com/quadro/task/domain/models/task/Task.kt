package com.quadro.task.domain.models.task

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class TaskStatus {
    BACKLOG, TODO, IN_PROGRESS, IN_REVIEW, DONE, CANCELLED;

    fun isTerminal() = this in setOf(DONE, CANCELLED)
    fun canTransitionTo(target: TaskStatus): Boolean = when (this) {
        BACKLOG -> target in setOf(TODO)
        TODO -> target in setOf(IN_PROGRESS, CANCELLED, BACKLOG)
        IN_PROGRESS -> target in setOf(IN_REVIEW, TODO, CANCELLED)
        IN_REVIEW -> target in setOf(DONE, IN_PROGRESS)
        DONE -> target in setOf(IN_PROGRESS)
        CANCELLED -> target in setOf(BACKLOG, TODO)
    }
}

@Serializable
enum class TaskPriority { LOW, MEDIUM, HIGH, CRITICAL }

@Serializable
enum class TaskType { STORY, TASK, BUG, EPIC, SUBTASK }

data class Task(
    val id: UUID,
    val projectId: UUID,
    val sprintId: UUID?,
    val parentTaskId: UUID?,
    val number: Int,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val type: TaskType,
    val assigneeId: UUID?,
    val assignedTeamId: UUID?,
    val reporterId: UUID,
    val storyPoints: Int?,
    val estimatedHours: Double?,
    val loggedHours: Double?,
    val dueDate: Instant?,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val labels: List<String> = emptyList(),
    val commentCount: Int = 0,
    val comments: List<TaskComment> = emptyList()
)

data class TaskCreate(
    val title: String,
    val projectId: UUID,
    val description: String? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val type: TaskType = TaskType.TASK,
    val assigneeId: UUID? = null,
    val assignedTeamId: UUID? = null,
    val sprintId: UUID? = null,
    val parentTaskId: UUID? = null,
    val storyPoints: Int? = null,
    val estimatedHours: Double? = null,
    val dueDate: Instant? = null,
    val labels: List<String> = emptyList(),
) {
    fun validate() {
        require(title.isNotBlank()) { "Task title cannot be blank" }
        require(title.length in 2..200) { "Title must be 2-200 chars" }
        storyPoints?.let { require(it in 1..100) { "Story points must be 1-100" } }
        estimatedHours?.let { require(it > 0) { "Estimated hours must be positive" } }
    }
}

data class TaskUpdate(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val assigneeId: UUID? = null,
    val assignedTeamId: UUID? = null,
    val sprintId: UUID? = null,
    val storyPoints: Int? = null,
    val estimatedHours: Double? = null,
    val loggedHours: Double? = null,
    val dueDate: Instant? = null,
    val labels: List<String>? = null,
)

data class TaskComment(
    val id: UUID,
    val taskId: UUID,
    val authorId: UUID,
    val content: String,
    val parentId: UUID?,
    val isEdited: Boolean,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val mentions: List<UUID> = emptyList(),
)

data class TaskCommentCreate(
    val content: String,
    val parentCommentId: UUID? = null,
    val mentionedUserIds: List<UUID> = emptyList(),
) {
    fun validate() {
        require(content.isNotBlank()) { "Comment cannot be blank" }
        require(content.length <= 10_000) { "Comment must be at most 10000 chars" }
    }
}

data class TaskCommentUpdate(
    val content: String,
) {
    fun validate() {
        require(content.isNotBlank()) { "Comment cannot be blank" }
        require(content.length <= 10_000) { "Comment must be at most 10000 chars" }
    }
}

data class TaskAttachment(
    val id: UUID,
    val taskId: UUID,
    val uploadedBy: UUID,
    val fileName: String,
    val fileSize: Long,
    val url: String,
    val createdAt: Instant
)

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