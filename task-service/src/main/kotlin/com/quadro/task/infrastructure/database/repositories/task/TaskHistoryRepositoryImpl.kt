package com.quadro.task.infrastructure.database.repositories.task

import com.quadro.task.domain.models.task.TaskHistory
import com.quadro.task.domain.repositories.task.TaskHistoryRepository
import com.quadro.task.infrastructure.database.entities.task.TaskHistoryEntity
import com.quadro.task.infrastructure.database.entities.task.TaskHistoryTable
import com.quadro.task.infrastructure.database.mappers.task.TaskHistoryMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TaskHistoryRepositoryImpl : TaskHistoryRepository {
    override suspend fun findById(id: UUID): TaskHistory? = newSuspendedTransaction {
        TaskHistoryEntity.findById(id)?.let(TaskHistoryMapper::toDomain)
    }

    override suspend fun findByTask(
        taskId: UUID,
        limit: Int,
        offset: Int
    ): List<TaskHistory> = newSuspendedTransaction {
        TaskHistoryEntity.find { TaskHistoryTable.taskId eq taskId }
            .limit(limit).offset(offset.toLong())
            .map(TaskHistoryMapper::toDomain)
    }

    override suspend fun create(history: TaskHistory): TaskHistory = newSuspendedTransaction {
        val entity = TaskHistoryMapper.toEntity(history)
        TaskHistoryMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Unit = newSuspendedTransaction {
        TaskHistoryEntity.findById(id)?.delete()
    }
}