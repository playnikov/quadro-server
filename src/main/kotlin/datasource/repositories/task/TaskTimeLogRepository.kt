package com.quadro.datasource.repositories.task

import com.quadro.domain.models.task.TaskTimeLog
import com.quadro.domain.models.task.TaskTimeStats
import java.util.UUID

interface TaskTimeLogRepository {
    suspend fun create(timeLog: TaskTimeLog): TaskTimeLog
    suspend fun findById(id: UUID): TaskTimeLog?
    suspend fun findByTask(taskId: UUID): List<TaskTimeLog>
    suspend fun findByUser(userId: UUID, from: Long?, to: Long?): List<TaskTimeLog>
    suspend fun delete(id: UUID): Boolean
    suspend fun deleteByTask(taskId: UUID): Int
    suspend fun getStats(taskId: UUID): TaskTimeStats
    suspend fun getTotalTimeByUser(userId: UUID, from: Long, to: Long): Long
}