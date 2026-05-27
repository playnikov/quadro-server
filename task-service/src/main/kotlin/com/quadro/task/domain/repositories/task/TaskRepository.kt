package com.quadro.task.domain.repositories.task

import com.quadro.task.domain.models.task.DurationPercentiles
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import java.util.UUID
import kotlin.time.Instant

interface TaskRepository {
    // CRUD
    suspend fun findById(id: UUID): Task?
    suspend fun findByProject(projectId: UUID, limit: Int, offset: Int): List<Task>
    suspend fun findBySprint(sprintId: UUID): List<Task>
    suspend fun findByAssignee(userId: UUID): List<Task>
    suspend fun findByParent(parentTaskId: UUID): List<Task>
    suspend fun create(task: Task): Task
    suspend fun update(task: Task): Task
    suspend fun delete(id: UUID)
    suspend fun clearAssignee(userId: UUID)
    suspend fun nextNumber(projectId: UUID): Int
    suspend fun findUpcomingDeadlines(projectId: UUID, from: Instant, to: Instant, limit: Int): List<Task>

    // Счётчики
    suspend fun countByProject(projectId: UUID): Long
    suspend fun countByStatus(projectId: UUID, status: TaskStatus): Long
    suspend fun countGroupedByStatus(projectId: UUID): Map<TaskStatus, Long>

    // Счётчики за период
    suspend fun countByStatusAndPeriod(projectId: UUID, status: TaskStatus, from: Instant, to: Instant): Long

    suspend fun countCreatedByPeriod(projectId: UUID, from: Instant, to: Instant): Long
    suspend fun countCompletedByPeriod(projectId: UUID, from: Instant, to: Instant): Long

    // Velocity
    suspend fun sumStoryPointsCompletedInPeriod(projectId: UUID, from: Instant, to: Instant): Double?
    suspend fun sumEstimatedHoursCompletedInPeriod(projectId: UUID, from: Instant, to: Instant): Double?

    // Overdue с пагинацией
    suspend fun findOverduePaginated(projectId: UUID, now: Instant, page: Int, size: Int): List<Task>
    suspend fun countOverdue(projectId: UUID, now: Instant): Long

    // Временные метрики
    suspend fun avgCompletionDaysInPeriod(projectId: UUID, from: Instant?, to: Instant?): Double
    suspend fun averageWipInPeriod(projectId: UUID, from: Instant, to: Instant): Double

    // Группировка по дням
    suspend fun getTasksCreatedGroupedByDay(projectId: UUID, from: Instant, to: Instant): Map<Instant, Long>
    suspend fun getTasksProgressGroupedByDay(projectId: UUID, from: Instant, to: Instant): Map<Instant, Long>
    suspend fun getTasksCompletedGroupedByDay(projectId: UUID, from: Instant, to: Instant): Map<Instant, Long>
}