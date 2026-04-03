package com.quadro.datasource.repositories.task

import com.quadro.datasource.entities.TaskCommentEntity
import com.quadro.datasource.entities.TaskCommentsTable
import com.quadro.datasource.mappers.TaskCommentMapper
import com.quadro.domain.models.task.TaskComment
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class TaskCommentRepositoryImpl : TaskCommentRepository {
    override suspend fun create(comment: TaskComment): TaskComment = newSuspendedTransaction {
        TaskCommentMapper.toDomain(TaskCommentMapper.toEntity(comment))
    }

    override suspend fun findById(id: UUID): TaskComment? = newSuspendedTransaction {
        TaskCommentEntity.findById(id)?.let { TaskCommentMapper.toDomain(it) }
    }

    override suspend fun findByTask(
        taskId: UUID,
        limit: Int,
        offset: Int
    ): List<TaskComment> = newSuspendedTransaction {
        TaskCommentEntity.find { TaskCommentsTable.taskId eq taskId }
            .orderBy(TaskCommentsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { TaskCommentMapper.toDomain(it) }
    }

    override suspend fun update(
        id: UUID,
        content: String
    ): TaskComment? = newSuspendedTransaction {
        val entity = TaskCommentEntity.findById(id) ?: return@newSuspendedTransaction null
        entity.content = content
        entity.updatedAt = Instant.now()
        TaskCommentMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        TaskCommentEntity.findById(id)?.delete() != null
    }

    override suspend fun countByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskCommentEntity.find { TaskCommentsTable.taskId eq taskId }.count()
    }

    override suspend fun deleteByTask(taskId: UUID): Int = newSuspendedTransaction {
        val comments = TaskCommentEntity.find { TaskCommentsTable.taskId eq taskId }.toList()
        comments.forEach { it.delete() }
        comments.size
    }

    override suspend fun getLastComment(taskId: UUID): TaskComment? = newSuspendedTransaction {
        TaskCommentEntity.find { TaskCommentsTable.taskId eq taskId }
            .orderBy(TaskCommentsTable.createdAt to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.let { TaskCommentMapper.toDomain(it) }
    }
}