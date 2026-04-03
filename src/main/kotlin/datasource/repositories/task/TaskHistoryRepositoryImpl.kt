package com.quadro.datasource.repositories.task

import com.quadro.datasource.entities.TaskHistoryEntity
import com.quadro.datasource.entities.TaskHistoryTable
import com.quadro.datasource.mappers.TaskHistoryMapper
import com.quadro.domain.models.task.TaskHistory
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TaskHistoryRepositoryImpl : TaskHistoryRepository {
    override suspend fun log(history: TaskHistory): TaskHistory = newSuspendedTransaction {
        TaskHistoryMapper.toDomain(TaskHistoryMapper.toEntity(history))
    }

    override suspend fun findByTask(
        taskId: UUID,
        limit: Int,
        offset: Int
    ): List<TaskHistory> = newSuspendedTransaction {
        TaskHistoryEntity.find { TaskHistoryTable.taskId eq taskId }
            .orderBy(TaskHistoryTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { TaskHistoryMapper.toDomain(it) }
    }

    override suspend fun countByTask(taskId: UUID): Long = newSuspendedTransaction {
        TaskHistoryEntity.find { TaskHistoryTable.taskId eq taskId }.count()
    }

    override suspend fun deleteByTask(taskId: UUID): Int = newSuspendedTransaction {
        val history = TaskHistoryEntity.find { TaskHistoryTable.taskId eq taskId }.toList()
        history.forEach { it.delete() }
        history.size
    }
}