package com.quadro.task.infrastructure.database.repositories.task

import com.quadro.shared.utils.toKotlinInstant
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.DurationPercentiles
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.infrastructure.database.entities.task.TaskEntity
import com.quadro.task.infrastructure.database.entities.task.TasksTable
import com.quadro.task.infrastructure.database.mappers.task.TaskMapper
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.and
import kotlin.time.Duration
import kotlin.time.Instant
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.slice

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

    override suspend fun countGroupedByStatus(projectId: UUID): Map<TaskStatus, Long> = newSuspendedTransaction {
        TasksTable
            .select(TasksTable.status, TasksTable.id.count())
            .where { TasksTable.projectId eq projectId }
            .groupBy(TasksTable.status)
            .associate { row ->
                row[TasksTable.status] to row[TasksTable.id.count()]
            }
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

    override suspend fun sumStoryPointsCompletedInPeriod(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): Double? = newSuspendedTransaction {
        val ids = TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.status eq TaskStatus.DONE) and
                    (TasksTable.completedAt greaterEq from.toOffsetDateTime()) and
                    (TasksTable.completedAt lessEq to.toOffsetDateTime())
        }.map { it.id.value }
        if (ids.isEmpty()) return@newSuspendedTransaction null
        TaskEntity.find { TasksTable.id inList ids }
            .sumOf { it.storyPoints ?: 0 }
            .toDouble()
    }

    override suspend fun sumEstimatedHoursCompletedInPeriod(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): Double? = newSuspendedTransaction {
        val sumExpr = TasksTable.estimatedHours.sum()
        TasksTable
            .select(sumExpr)
            .where {
                (TasksTable.projectId eq projectId) and
                        (TasksTable.status eq TaskStatus.DONE) and
                        (TasksTable.completedAt greaterEq from.toOffsetDateTime()) and
                        (TasksTable.completedAt lessEq to.toOffsetDateTime())
            }
            .firstOrNull()
            ?.get(sumExpr)
    }

    override suspend fun findOverduePaginated(
        projectId: UUID,
        now: Instant,
        page: Int,
        size: Int
    ): List<Task> = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.dueDate less now.toOffsetDateTime()) and
                    (TasksTable.status neq TaskStatus.DONE)
        }
            .limit(size).offset(start = (page * size).toLong())
            .map { TaskMapper.toDomain(it) }
    }

    override suspend fun countOverdue(projectId: UUID, now: Instant): Long = newSuspendedTransaction {
        TaskEntity.find {
            (TasksTable.projectId eq projectId) and
                    (TasksTable.dueDate less now.toOffsetDateTime()) and
                    (TasksTable.status neq TaskStatus.DONE)
        }.count()
    }

    override suspend fun avgCompletionDaysInPeriod(
        projectId: UUID,
        from: Instant?,
        to: Instant?
    ): Double = newSuspendedTransaction {
        val conditions = mutableListOf<Op<Boolean>>()
        conditions.add(TasksTable.projectId eq projectId)
        conditions.add(TasksTable.status eq TaskStatus.DONE)
        conditions.add(TasksTable.completedAt.isNotNull())
        from?.let {
            conditions.add(TasksTable.completedAt greaterEq from.toOffsetDateTime())
        }
        to?.let {
            conditions.add(TasksTable.completedAt lessEq to.toOffsetDateTime())
        }

        val finalCondition = conditions.reduce { acc, op -> acc and op }

        val daysList = TaskEntity.find(finalCondition).map { entity ->
            ChronoUnit.DAYS.between(entity.createdAt, entity.completedAt)
        }
        if (daysList.isEmpty()) 0.0 else daysList.average()
    }

    override suspend fun averageWipInPeriod(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): Double = newSuspendedTransaction {
        val startDate = from.toOffsetDateTime()
        val endDate = to.toOffsetDateTime()
        val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
        var totalWip = 0L
        for (i in 0 until days) {
            val dayStart = startDate.plusDays(i)
            val dayEnd = dayStart.plusDays(1)
            val wip = TaskEntity.find {
                (TasksTable.projectId eq projectId) and
                        (TasksTable.status notInList listOf(TaskStatus.DONE, TaskStatus.CANCELLED)) and
                        (TasksTable.createdAt lessEq dayEnd) and
                        ((TasksTable.completedAt greaterEq dayStart) or (TasksTable.completedAt.isNull()))
            }.count()
            totalWip += wip
        }
        if (days.toInt() == 0) 0.0 else totalWip.toDouble() / days
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