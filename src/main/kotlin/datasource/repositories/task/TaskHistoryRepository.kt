package com.quadro.datasource.repositories.task

import com.quadro.domain.models.task.TaskHistory
import java.util.UUID

interface TaskHistoryRepository {
    suspend fun log(history: TaskHistory): TaskHistory
    suspend fun findByTask(taskId: UUID, limit: Int, offset: Int): List<TaskHistory>
    suspend fun countByTask(taskId: UUID): Long
    suspend fun deleteByTask(taskId: UUID): Int
}