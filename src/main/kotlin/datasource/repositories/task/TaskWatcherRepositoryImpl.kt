package com.quadro.datasource.repositories.task

import com.quadro.datasource.entities.TaskWatcherEntity
import com.quadro.datasource.entities.TaskWatchersTable
import com.quadro.datasource.mappers.TaskWatcherMapper
import com.quadro.domain.models.task.NotificationLevel
import com.quadro.domain.models.task.TaskWatcher
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.*

class TaskWatcherRepositoryImpl : TaskWatcherRepository {
    override suspend fun addWatcher(
        taskId: UUID,
        userId: UUID,
        level: NotificationLevel
    ): TaskWatcher = newSuspendedTransaction {
        val entity = TaskWatcherEntity.new {
            this.taskId = taskId
            this.userId = userId
            this.addedAt = Instant.now()
            this.notificationLevel = level
        }
        TaskWatcherMapper.toDomain(entity)
    }

    override suspend fun removeWatcher(taskId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        val watcher = TaskWatcherEntity.find {
            (TaskWatchersTable.taskId eq taskId) and
                    (TaskWatchersTable.userId eq userId)
        }.firstOrNull()
        watcher?.delete() != null
    }

    override suspend fun findByTask(taskId: UUID): List<TaskWatcher> = newSuspendedTransaction {
        TaskWatcherEntity.find { TaskWatchersTable.taskId eq taskId }
            .map { TaskWatcherMapper.toDomain(it) }
    }

    override suspend fun findByUser(userId: UUID): List<TaskWatcher> = newSuspendedTransaction {
        TaskWatcherEntity.find { TaskWatchersTable.userId eq userId }
            .map { TaskWatcherMapper.toDomain(it) }
    }

    override suspend fun isWatching(taskId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        !TaskWatcherEntity.find {
            (TaskWatchersTable.taskId eq taskId) and
                    (TaskWatchersTable.userId eq userId)
        }.empty()
    }

    override suspend fun countByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskWatcherEntity.find { TaskWatchersTable.taskId eq taskId }.count()
    }

    override suspend fun updateNotificationLevel(
        taskId: UUID,
        userId: UUID,
        level: NotificationLevel
    ): Boolean = newSuspendedTransaction {
        val watcher = TaskWatcherEntity.find {
            (TaskWatchersTable.taskId eq taskId) and
                    (TaskWatchersTable.userId eq userId)
        }.firstOrNull()
        watcher?.apply {
            notificationLevel = level
        } != null
    }

    override suspend fun removeAllByTask(taskId: UUID): Int = newSuspendedTransaction {
        val watchers = TaskWatcherEntity.find { TaskWatchersTable.taskId eq taskId }.toList()
        watchers.forEach { it.delete() }
        watchers.size
    }
}