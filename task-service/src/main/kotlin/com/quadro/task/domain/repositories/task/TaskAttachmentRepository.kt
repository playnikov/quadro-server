package com.quadro.task.domain.repositories.task

import com.quadro.task.domain.models.task.TaskAttachment
import java.util.UUID

interface TaskAttachmentRepository {
    suspend fun findById(id: UUID): TaskAttachment?
    suspend fun findByTask(taskId: UUID): List<TaskAttachment>
    suspend fun create(attachment: TaskAttachment): TaskAttachment
    suspend fun delete(id: UUID)
    suspend fun countByTask(taskId: UUID): Long
    suspend fun totalSizeByTask(taskId: UUID): Long
}