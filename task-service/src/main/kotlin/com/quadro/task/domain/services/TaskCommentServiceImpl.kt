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
    override suspend fun createComment(commentCreate: TaskCommentCreate): TaskComment {
        commentCreate.validate()
        val task = taskRepository.findById(commentCreate.taskId)
            ?: throw DomainException.NotFound("Task", commentCreate.taskId.toString())

        if (commentCreate.parentId != null) {
            val parent = commentRepository.findById(commentCreate.parentId)
                ?: throw DomainException.NotFound("Parent comment", commentCreate.parentId.toString())
            if (parent.taskId != task.id) {
                throw DomainException.BusinessRule("Parent comment belongs to a different task")
            }
        }

        val now = Clock.System.now()
        val comment = TaskComment(
            id = UUID.randomUUID(),
            taskId = commentCreate.taskId,
            authorId = commentCreate.authorId,
            content = commentCreate.content,
            parentId = commentCreate.parentId,
            isEdited = false,
            isDeleted = false,
            mentions = commentCreate.mentions,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        val saved = commentRepository.create(comment)

        taskHistoryService.recordCommentAdded(commentCreate.taskId, commentCreate.authorId, saved.id)
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

    override suspend fun deleteComment(commentId: UUID, userId: UUID) {
        val comment = commentRepository.findById(commentId)
            ?: throw DomainException.NotFound("Comment", commentId.toString())
        if (comment.authorId != userId) {
            throw DomainException.Forbidden("You can only delete your own comments")
        }
        commentRepository.softDelete(commentId)
    }

    override suspend fun getComment(commentId: UUID): TaskComment? =
        commentRepository.findById(commentId)

    override suspend fun getCommentsByTask(taskId: UUID): List<TaskComment> =
        commentRepository.findByTask(taskId).filterNot { it.isDeleted }

    override suspend fun getReplies(parentId: UUID): List<TaskComment> =
        commentRepository.findReplies(parentId).filterNot { it.isDeleted }

    override suspend fun countByTask(taskId: UUID): Long =
        commentRepository.countByTask(taskId)
}