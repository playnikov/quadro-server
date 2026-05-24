package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.TaskComment
import com.quadro.task.domain.models.task.TaskCommentCreate
import com.quadro.task.domain.models.task.TaskCommentUpdate
import java.util.UUID

interface TaskCommentService {
    suspend fun addComment(taskId: UUID, authorId: UUID, create: TaskCommentCreate): TaskComment
    suspend fun updateComment(commentId: UUID, userId: UUID, update: TaskCommentUpdate): TaskComment
    suspend fun deleteComment(commentId: UUID, userId: UUID, isAdmin: Boolean)
    suspend fun getCommentsForTask(taskId: UUID): List<TaskComment>
    suspend fun getReplies(commentId: UUID): List<TaskComment>
}