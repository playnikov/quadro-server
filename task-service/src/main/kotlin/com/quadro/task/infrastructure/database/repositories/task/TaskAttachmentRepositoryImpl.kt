package com.quadro.task.infrastructure.database.repositories.task

import com.quadro.task.domain.models.task.TaskAttachment
import com.quadro.task.domain.repositories.task.TaskAttachmentRepository
import com.quadro.task.infrastructure.database.entities.task.TaskAttachmentEntity
import com.quadro.task.infrastructure.database.entities.task.TaskAttachmentsTable
import com.quadro.task.infrastructure.database.entities.task.TaskEntity
import com.quadro.task.infrastructure.database.mappers.task.TaskAttachmentMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TaskAttachmentRepositoryImpl : TaskAttachmentRepository {
    override suspend fun findById(id: UUID): TaskAttachment? = newSuspendedTransaction {
        TaskAttachmentEntity.findById(id)?.let(TaskAttachmentMapper::toDomain)
    }

    override suspend fun findByTask(taskId: UUID): List<TaskAttachment> = newSuspendedTransaction {
        TaskAttachmentEntity.find { TaskAttachmentsTable.taskId eq taskId }
            .map(TaskAttachmentMapper::toDomain)
    }

    override suspend fun create(attachment: TaskAttachment): TaskAttachment = newSuspendedTransaction {
        val entity = TaskAttachmentMapper.toEntity(attachment)
        TaskAttachmentMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Unit = newSuspendedTransaction {
        TaskEntity.findById(id)?.delete()
    }

    override suspend fun countByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskAttachmentEntity.find { TaskAttachmentsTable.taskId eq taskId }.count()
    }

    override suspend fun totalSizeByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskAttachmentEntity.find { TaskAttachmentsTable.taskId eq taskId }
            .sumOf { it.fileSize }
    }
}