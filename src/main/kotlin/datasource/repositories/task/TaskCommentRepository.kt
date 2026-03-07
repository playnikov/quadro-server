package com.quadro.datasource.repositories.task

import com.quadro.domain.models.task.TaskComment
import java.util.UUID

interface TaskCommentRepository {
    suspend fun create(comment: TaskComment): TaskComment
    suspend fun findById(id: UUID): TaskComment?
    suspend fun findByTask(taskId: UUID, limit: Int, offset: Int): List<TaskComment>
    suspend fun update(id: UUID, content: String): TaskComment?
    suspend fun delete(id: UUID): Boolean
    suspend fun countByTask(taskId: UUID): Long
    suspend fun deleteByTask(taskId: UUID): Int
    suspend fun getLastComment(taskId: UUID): TaskComment?
}