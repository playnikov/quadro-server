package com.quadro.datasource.repositories.task

import com.quadro.datasource.entities.TaskAttachmentEntity
import com.quadro.datasource.entities.TaskAttachmentsTable
import com.quadro.datasource.mappers.TaskAttachmentMapper
import com.quadro.domain.models.task.TaskAttachment
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TaskAttachmentRepositoryImpl : TaskAttachmentRepository {
    override suspend fun create(attachment: TaskAttachment): TaskAttachment = newSuspendedTransaction {
        TaskAttachmentMapper.toDomain(TaskAttachmentMapper.toEntity(attachment))
    }

    override suspend fun findById(id: UUID): TaskAttachment? = newSuspendedTransaction {
        TaskAttachmentEntity.findById(id)?.let { TaskAttachmentMapper.toDomain(it) }
    }

    override suspend fun findByTask(taskId: UUID): List<TaskAttachment> = newSuspendedTransaction {
        TaskAttachmentEntity.find { TaskAttachmentsTable.taskId eq taskId }
            .orderBy(TaskAttachmentsTable.createdAt to SortOrder.DESC)
            .map { TaskAttachmentMapper.toDomain(it) }
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        TaskAttachmentEntity.findById(id)?.delete() != null
    }

    override suspend fun deleteByTask(taskId: UUID): Int = newSuspendedTransaction {
        val attachments = TaskAttachmentEntity.find { TaskAttachmentsTable.taskId eq taskId }.toList()
        attachments.forEach { it.delete() }
        attachments.size
    }

    override suspend fun countByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskAttachmentEntity.find { TaskAttachmentsTable.taskId eq taskId }.count()
    }

    override suspend fun getTotalSizeByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskAttachmentEntity.find { TaskAttachmentsTable.taskId eq taskId }
            .sumOf { it.fileSize }
    }
}