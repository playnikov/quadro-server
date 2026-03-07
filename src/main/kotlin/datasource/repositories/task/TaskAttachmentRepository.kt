package com.quadro.datasource.repositories.task

import com.quadro.domain.models.task.TaskAttachment
import java.util.UUID

interface TaskAttachmentRepository {
    suspend fun create(attachment: TaskAttachment): TaskAttachment
    suspend fun findById(id: UUID): TaskAttachment?
    suspend fun findByTask(taskId: UUID): List<TaskAttachment>
    suspend fun delete(id: UUID): Boolean
    suspend fun deleteByTask(taskId: UUID): Int
    suspend fun countByTask(taskId: UUID): Long
    suspend fun getTotalSizeByTask(taskId: UUID): Long
}