package com.quadro.task.domain.repositories.task

import com.quadro.task.domain.models.task.TaskComment
import java.util.UUID

interface TaskCommentRepository {
    suspend fun findById(id: UUID): TaskComment?
    suspend fun findByTask(taskId: UUID): List<TaskComment>
    suspend fun findReplies(parentId: UUID): List<TaskComment>
    suspend fun create(comment: TaskComment): TaskComment
    suspend fun update(comment: TaskComment): TaskComment
    suspend fun softDelete(id: UUID): Boolean
    suspend fun countByTask(taskId: UUID): Long
}