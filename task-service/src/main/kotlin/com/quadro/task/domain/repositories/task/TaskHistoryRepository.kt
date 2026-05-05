package com.quadro.task.domain.repositories.task

import com.quadro.task.domain.models.task.TaskHistory
import java.util.UUID

interface TaskHistoryRepository {
    suspend fun findById(id: UUID): TaskHistory?
    suspend fun findByTask(taskId: UUID, limit: Int, offset: Int): List<TaskHistory>
    suspend fun create(history: TaskHistory): TaskHistory
    suspend fun delete(id: UUID)
}