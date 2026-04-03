package com.quadro.datasource.repositories.task

import com.quadro.datasource.entities.TaskEntity
import com.quadro.datasource.entities.TaskTimeLogEntity
import com.quadro.datasource.entities.TaskTimeLogsTable
import com.quadro.datasource.mappers.TaskTimeLogMapper
import com.quadro.domain.models.task.TaskTimeLog
import com.quadro.domain.models.task.TaskTimeStats
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.collections.filter

class TaskTimeLogRepositoryImpl : TaskTimeLogRepository {
    override suspend fun create(timeLog: TaskTimeLog): TaskTimeLog = newSuspendedTransaction {
        TaskTimeLogMapper.toDomain(TaskTimeLogMapper.toEntity(timeLog))
    }

    override suspend fun findById(id: UUID): TaskTimeLog? = newSuspendedTransaction {
        TaskTimeLogEntity.findById(id)?.let { TaskTimeLogMapper.toDomain(it) }
    }

    override suspend fun findByTask(taskId: UUID): List<TaskTimeLog> = newSuspendedTransaction {
        TaskTimeLogEntity.find { TaskTimeLogsTable.taskId eq taskId }
            .orderBy(TaskTimeLogsTable.loggedAt to SortOrder.DESC)
            .map { TaskTimeLogMapper.toDomain(it) }
    }

    override suspend fun findByUser(userId: UUID, from: Long?, to: Long?): List<TaskTimeLog> = newSuspendedTransaction {
        val query = TaskTimeLogEntity.find { TaskTimeLogsTable.userId eq userId }

        val filtered = if (from != null || to != null) {
            query.filter { entity ->
                var matches = true
                if (from != null) {
                    matches = matches && entity.loggedAt.toEpochMilli() >= from
                }
                if (to != null) {
                    matches = matches && entity.loggedAt.toEpochMilli() <= to
                }
                matches
            }
        } else {
            query.toList()
        }

        filtered.sortedByDescending { it.loggedAt }
            .map { TaskTimeLogMapper.toDomain(it) }
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        TaskTimeLogEntity.findById(id)?.delete() != null
    }

    override suspend fun deleteByTask(taskId: UUID): Int = newSuspendedTransaction {
        val logs = TaskTimeLogEntity.find { TaskTimeLogsTable.taskId eq taskId }.toList()
        logs.forEach { it.delete() }
        logs.size
    }

    override suspend fun getStats(taskId: UUID): TaskTimeStats = newSuspendedTransaction {
        val logs = TaskTimeLogEntity.find { TaskTimeLogsTable.taskId eq taskId }

        val totalTimeSpent = logs.sumOf { it.timeSpent }

        val task = TaskEntity.findById(taskId)!!
        val timeEstimate = task.timeEstimate

        val remainingTime = if (timeEstimate != null) {
            timeEstimate - totalTimeSpent
        } else null

        val now = System.currentTimeMillis()
        val todayStart = now - (now % 86400000)
        val weekStart = now - (7 * 86400000)

        val timeSpentToday = logs.filter { it.loggedAt.toEpochMilli() >= todayStart }.sumOf { it.timeSpent }
        val timeSpentThisWeek = logs.filter { it.loggedAt.toEpochMilli() >= weekStart }.sumOf { it.timeSpent }

        TaskTimeStats(
            totalTimeSpent = totalTimeSpent,
            timeEstimate = timeEstimate,
            remainingTime = remainingTime,
            timeSpentToday = timeSpentToday,
            timeSpentThisWeek = timeSpentThisWeek,
            logsCount = logs.count().toInt()
        )
    }

    override suspend fun getTotalTimeByUser(userId: UUID, from: Long, to: Long): Long {
        TODO("No getTotalTimeByUser")
    }
}