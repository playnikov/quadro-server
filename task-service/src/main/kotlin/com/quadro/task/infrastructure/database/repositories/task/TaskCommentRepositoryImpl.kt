package com.quadro.task.infrastructure.database.repositories.task

import com.quadro.shared.dto.DomainException
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.TaskComment
import com.quadro.task.domain.repositories.task.TaskCommentRepository
import com.quadro.task.infrastructure.database.entities.task.TaskCommentEntity
import com.quadro.task.infrastructure.database.entities.task.TaskCommentsTable
import com.quadro.task.infrastructure.database.entities.task.TaskEntity
import com.quadro.task.infrastructure.database.mappers.task.TaskCommentMapper
import com.quadro.task.infrastructure.database.mappers.task.TaskMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Clock

class TaskCommentRepositoryImpl : TaskCommentRepository {
    override suspend fun findById(id: UUID): TaskComment? = newSuspendedTransaction {
        TaskCommentEntity.findById(id)?.let(TaskCommentMapper::toDomain)
    }

    override suspend fun findByTask(taskId: UUID): List<TaskComment> = newSuspendedTransaction {
        TaskCommentEntity.find { TaskCommentsTable.taskId eq taskId }
            .map(TaskCommentMapper::toDomain)
    }

    override suspend fun findReplies(parentId: UUID): List<TaskComment> = newSuspendedTransaction {
        TaskCommentEntity.find { TaskCommentsTable.parentId eq parentId }
            .map(TaskCommentMapper::toDomain)
    }

    override suspend fun create(comment: TaskComment): TaskComment = newSuspendedTransaction {
        val entity = TaskCommentMapper.toEntity(comment)
        TaskCommentMapper.toDomain(entity)
    }

    override suspend fun update(comment: TaskComment): TaskComment = newSuspendedTransaction {
        TaskCommentEntity.findById(comment.id)?.apply {
            this.content = comment.content
            this.updatedAt = Clock.System.now().toOffsetDateTime()
        }?.let(TaskCommentMapper::toDomain)
            ?: throw DomainException.NotFound("Comment", comment.id.toString())
    }

    override suspend fun softDelete(id: UUID): Boolean = newSuspendedTransaction {
        TaskCommentEntity.findById(id)?.apply {
            this.isDeleted = true
            this.updatedAt = Clock.System.now().toOffsetDateTime()
        } != null
    }

    override suspend fun countByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskCommentEntity.find { TaskCommentsTable.taskId eq taskId }.count()
    }
}