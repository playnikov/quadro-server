package com.quadro.task.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.TaskComment
import com.quadro.task.domain.models.task.TaskCommentCreate
import com.quadro.task.domain.models.task.TaskCommentUpdate
import com.quadro.task.domain.repositories.task.TaskCommentRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import java.util.UUID
import kotlin.time.Clock

class TaskCommentServiceImpl(
    private val commentRepository: TaskCommentRepository,
    private val taskRepository: TaskRepository,
    private val taskHistoryService: TaskHistoryService
) : TaskCommentService {
    override suspend fun addComment(
        taskId: UUID,
        authorId: UUID,
        create: TaskCommentCreate
    ): TaskComment {
        create.validate()
        val task = taskRepository.findById(taskId)
            ?: throw DomainException.NotFound("Task", taskId.toString())

        if (create.parentCommentId != null) {
            val parent = commentRepository.findById(create.parentCommentId)
                ?: throw DomainException.NotFound("Parent comment", create.parentCommentId.toString())
            if (parent.taskId != taskId) {
                throw DomainException.BusinessRule("Parent comment belongs to a different task")
            }
        }

        val now = Clock.System.now()
        val comment = TaskComment(
            id = UUID.randomUUID(),
            taskId = taskId,
            authorId = authorId,
            content = create.content,
            parentId = create.parentCommentId,
            isEdited = false,
            isDeleted = false,
            createdAt = now,
            updatedAt = now,
            mentions = create.mentionedUserIds
        )
        val saved = commentRepository.create(comment)

        taskHistoryService.recordCommentAdded(taskId, authorId, saved.id)
        return saved
    }

    override suspend fun updateComment(
        commentId: UUID,
        userId: UUID,
        update: TaskCommentUpdate
    ): TaskComment {
        update.validate()
        val comment = commentRepository.findById(commentId)
            ?: throw DomainException.NotFound("Comment", commentId.toString())
        if (comment.authorId != userId) {
            throw DomainException.Forbidden("Only author can edit comment")
        }
        if (comment.isDeleted) {
            throw DomainException.BusinessRule("Cannot edit deleted comment")
        }

        val updated = comment.copy(
            content = update.content,
            isEdited = true,
            updatedAt = Clock.System.now()
        )
        return commentRepository.update(updated)
    }

    override suspend fun deleteComment(commentId: UUID, userId: UUID, isAdmin: Boolean) {
        val comment = commentRepository.findById(commentId)
            ?: throw DomainException.NotFound("Comment", commentId.toString())
        if (comment.authorId != userId && !isAdmin) {
            throw DomainException.Forbidden("Only author or admin can delete comment")
        }
        commentRepository.softDelete(commentId)
    }

    override suspend fun getCommentsForTask(taskId: UUID): List<TaskComment> {
        return commentRepository.findByTask(taskId)
    }

    override suspend fun getReplies(commentId: UUID): List<TaskComment> {
        return commentRepository.findReplies(commentId)
    }
}