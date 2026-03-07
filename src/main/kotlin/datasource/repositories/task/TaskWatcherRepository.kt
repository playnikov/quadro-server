package com.quadro.datasource.repositories.task

import com.quadro.domain.models.task.NotificationLevel
import com.quadro.domain.models.task.TaskWatcher
import java.util.UUID

interface TaskWatcherRepository {
    suspend fun addWatcher(taskId: UUID, userId: UUID, level: NotificationLevel): TaskWatcher
    suspend fun removeWatcher(taskId: UUID, userId: UUID): Boolean
    suspend fun findByTask(taskId: UUID): List<TaskWatcher>
    suspend fun findByUser(userId: UUID): List<TaskWatcher>
    suspend fun isWatching(taskId: UUID, userId: UUID): Boolean
    suspend fun countByTask(taskId: UUID): Long
    suspend fun updateNotificationLevel(taskId: UUID, userId: UUID, level: NotificationLevel): Boolean
    suspend fun removeAllByTask(taskId: UUID): Int
}