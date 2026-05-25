package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.TaskComment
import com.quadro.task.domain.models.task.TaskCommentCreate
import com.quadro.task.domain.models.task.TaskCommentUpdate
import java.util.UUID

interface TaskCommentService {
    suspend fun createComment(commentCreate: TaskCommentCreate): TaskComment
    suspend fun updateComment(commentId: UUID, userId: UUID, update: TaskCommentUpdate): TaskComment
    suspend fun deleteComment(commentId: UUID, userId: UUID)
    suspend fun getComment(commentId: UUID): TaskComment?
    suspend fun getCommentsByTask(taskId: UUID): List<TaskComment>
    suspend fun getReplies(parentId: UUID): List<TaskComment>
    suspend fun countByTask(taskId: UUID): Long
}