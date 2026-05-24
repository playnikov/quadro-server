package com.quadro.task.infrastructure.database.repositories.task

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.infrastructure.database.entities.task.TaskEntity
import com.quadro.task.infrastructure.database.entities.task.TasksTable
import com.quadro.task.infrastructure.database.mappers.task.TaskMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.and
import kotlin.time.Duration
import kotlin.time.Instant

class TaskRepositoryImpl : TaskRepository {
    override suspend fun findById(id: UUID): Task? = newSuspendedTransaction {
        TaskEntity.findById(id)?.let(TaskMapper::toDomain)
    }

    override suspend fun findByProject(
        projectId: UUID,
        limit: Int,
        offset: Int
    ): List<Task> = newSuspendedTransaction {
        TaskEntity.find { TasksTable.projectId eq projectId }
            .limit(limit).offset(offset.toLong())
            .map(TaskMapper::toDomain)
    }

    override suspend fun findBySprint(sprintId: UUID): List<Task> = newSuspendedTransaction {
        TaskEntity.find { TasksTable.sprintId eq sprintId }
            .map(TaskMapper::toDomain)
    }

    override suspend fun findByAssignee(userId: UUID): List<Task> = newSuspendedTransaction {
        TaskEntity.find { TasksTable.assigneeId eq userId }
            .map(TaskMapper::toDomain)
    }

    override suspend fun findByParent(parentTaskId: UUID): List<Task> = newSuspendedTransaction {
        TaskEntity.find { TasksTable.parentTaskId eq parentTaskId }
            .map(TaskMapper::toDomain)
    }

    override suspend fun create(task: Task): Task = newSuspendedTransaction {
        val entity = TaskMapper.toEntity(task)
        TaskMapper.toDomain(entity)
    }

    override suspend fun update(task: Task): Task = newSuspendedTransaction {
        val entity = TaskEntity.findById(task.id)
            ?: throw IllegalArgumentException("Task not found with id: ${task.id}")
        TaskMapper.updateEntity(entity, task)
        TaskMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Unit = newSuspendedTransaction {
        TaskEntity.findById(id)?.delete()
    }

    override suspend fun clearAssignee(userId: UUID): Unit = newSuspendedTransaction {
        TaskEntity.find { TasksTable.assigneeId eq userId }.forEach {
            it.assigneeId = null
        }
    }

    override suspend fun nextNumber(projectId: UUID): Int = newSuspendedTransaction {
        TaskEntity.find { TasksTable.projectId eq projectId }
            .maxOfOrNull { it.number }?.plus(1) ?: 1
    }

    override suspend fun countByProject(projectId: UUID): Long = newSuspendedTransaction {
        TaskEntity.find { TasksTable.projectId eq projectId }.count()
    }

    override suspend fun countByStatus(
        projectId: UUID,
        status: TaskStatus
    ): Long = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.status eq status)
        }.count()
    }

    override suspend fun countByStatusAndPeriod(
        projectId: UUID,
        status: TaskStatus,
        from: Instant,
        to: Instant
    ): Long = newSuspendedTransaction {
        val column = when (status) {
            TaskStatus.DONE -> TasksTable.completedAt
            else -> TasksTable.updatedAt
        }
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.status eq status) and
                    (column greaterEq from.toOffsetDateTime()) and
                    (column lessEq to.toOffsetDateTime())
        }.count()
    }

    override suspend fun findOverdue(
        projectId: UUID,
        now: Instant
    ): List<Task> = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.dueDate less now.toOffsetDateTime()) and
                    (TasksTable.status neq TaskStatus.DONE)
        }.map(TaskMapper::toDomain)
    }

    override suspend fun avgCompletionDays(projectId: UUID): Double = newSuspendedTransaction {
        val completionDurations = TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.status eq TaskStatus.DONE) and
                    TasksTable.completedAt.isNotNull()
        }.map { entity ->
            val createdAt = entity.createdAt
            val completedAt = entity.completedAt
            ChronoUnit.DAYS.between(createdAt, completedAt)
        }


        if (completionDurations.isEmpty()) 0.0 else completionDurations.average()
    }

    override suspend fun countCreatedByPeriod(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): Long = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.createdAt greaterEq from.toOffsetDateTime()) and
                    (TasksTable.createdAt lessEq to.toOffsetDateTime())
        }.count()
    }

    override suspend fun countCompletedByPeriod(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): Long = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.status eq TaskStatus.DONE) and
                    (TasksTable.completedAt greaterEq from.toOffsetDateTime()) and
                    (TasksTable.completedAt lessEq to.toOffsetDateTime())
        }.count()
    }

    override suspend fun getTasksCreatedGroupedByDay(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): Map<Instant, Long> = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.createdAt greaterEq from.toOffsetDateTime()) and
                    (TasksTable.createdAt lessEq to.toOffsetDateTime())
        }.groupBy { entity ->
            entity.createdAt.toKotlinInstant()
        }.mapValues { it.value.size.toLong() }
    }

    override suspend fun getTasksCompletedGroupedByDay(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): Map<Instant, Long> = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.status eq TaskStatus.DONE) and
                    (TasksTable.completedAt greaterEq from.toOffsetDateTime()) and
                    (TasksTable.completedAt lessEq to.toOffsetDateTime())
        }.groupBy { entity ->
            entity.completedAt!!.toKotlinInstant()
        }.mapValues { it.value.size.toLong() }
    }
}