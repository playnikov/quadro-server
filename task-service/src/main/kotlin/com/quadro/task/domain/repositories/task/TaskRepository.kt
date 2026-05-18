package com.quadro.task.domain.repositories.task

import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import java.util.UUID
import kotlin.time.Instant

interface TaskRepository {
    suspend fun findById(id: UUID): Task?
    suspend fun findByProject(projectId: UUID, limit: Int, offset: Int): List<Task>
    suspend fun findBySprint(sprintId: UUID): List<Task>
    suspend fun findByAssignee(userId: UUID): List<Task>
    suspend fun findByTeam(teamId: UUID, projectId: UUID): List<Task>
    suspend fun findByParent(parentTaskId: UUID): List<Task>
    suspend fun create(task: Task): Task
    suspend fun update(task: Task): Task
    suspend fun delete(id: UUID)
    suspend fun nextNumber(projectId: UUID): Int
    suspend fun countByProject(projectId: UUID): Long
    suspend fun clearAssignedTeam(teamId: UUID)
    suspend fun clearAssignee(userId: UUID)
    suspend fun countByStatus(projectId: UUID, status: TaskStatus): Long
    suspend fun countByStatusAndPeriod(
        projectId: UUID,
        status: TaskStatus,
        from: Instant,
        to: Instant,
    ): Long
    suspend fun findOverdue(projectId: UUID, now: Instant): List<Task>
    suspend fun avgCompletionDays(projectId: UUID): Double

    suspend fun countCreatedByPeriod(projectId: UUID, from: Instant, to: Instant): Long
    suspend fun countCompletedByPeriod(projectId: UUID, from: Instant, to: Instant): Long
    suspend fun getTasksCreatedGroupedByDay(projectId: UUID, from: Instant, to: Instant): Map<Instant, Long>
    suspend fun getTasksCompletedGroupedByDay(projectId: UUID, from: Instant, to: Instant): Map<Instant, Long>
}