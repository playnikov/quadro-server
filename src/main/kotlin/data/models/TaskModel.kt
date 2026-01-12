package com.quadro.data.models

import java.time.LocalDateTime

data class TaskModel(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: LocalDateTime?,
    val projectId: Long,
    val assignedId: Long,
    val reportedId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class TaskStatus {
    TODO, IN_PROGRESS, REVIEW, DONE
}

enum class TaskPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}
