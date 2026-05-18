package com.quadro.task.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.PeriodReport
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.repositories.task.TaskRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class TaskReportingServiceImpl(
    private val taskRepository: TaskRepository
) : TaskReportingService {

    override suspend fun getBacklogCount(projectId: UUID): Long {
        return taskRepository.countByStatus(projectId, TaskStatus.BACKLOG)
    }

    override suspend fun getTodoCount(projectId: UUID): Long {
        return taskRepository.countByStatus(projectId, TaskStatus.TODO)
    }

    override suspend fun getInProgressCount(projectId: UUID): Long {
        return taskRepository.countByStatus(projectId, TaskStatus.IN_PROGRESS)
    }

    override suspend fun getInReviewCount(projectId: UUID): Long {
        return taskRepository.countByStatus(projectId, TaskStatus.IN_REVIEW)
    }

    override suspend fun getDoneCount(projectId: UUID): Long {
        return taskRepository.countByStatus(projectId, TaskStatus.DONE)
    }

    override suspend fun getCancelledCount(projectId: UUID): Long {
        return taskRepository.countByStatus(projectId, TaskStatus.CANCELLED)
    }

    override suspend fun getTaskCounts(projectId: UUID): Map<TaskStatus, Long> {
        return TaskStatus.entries.associateWith { status ->
            taskRepository.countByStatus(projectId, status)
        }
    }

    override suspend fun getOverdueTasks(projectId: UUID, now: Instant): List<Task> {
        return taskRepository.findOverdue(projectId, now)
    }

    override suspend fun getAverageCompletionDays(projectId: UUID): Double {
        return taskRepository.avgCompletionDays(projectId)
    }

    override suspend fun getCompletionRate(projectId: UUID): Double {
        val total = taskRepository.countByProject(projectId)
        if (total == 0L) return 0.0

        val completed = taskRepository.countByStatus(projectId, TaskStatus.DONE)
        return (completed.toDouble() / total) * 100
    }

    override suspend fun getVelocity(projectId: UUID): Double {
        val now = Clock.System.now()
        val oneWeekAgo = now - 7.days

        val completedLastWeek = taskRepository.countByStatusAndPeriod(
            projectId = projectId,
            status = TaskStatus.DONE,
            from = oneWeekAgo,
            to = now
        )

        return completedLastWeek.toDouble()
    }

    override suspend fun getPeriodReport(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): PeriodReport {
        val created = taskRepository.countCreatedByPeriod(projectId, from, to)
        val completed = taskRepository.countCompletedByPeriod(projectId, from, to)

        val statusBreakdown = TaskStatus.entries.associate { status ->
            status.name to taskRepository.countByStatusAndPeriod(projectId, status, from, to)
        }

        val dailyCreation = taskRepository.getTasksCreatedGroupedByDay(projectId, from, to)
            .mapKeys { it.key.toString() } // LocalDate -> "yyyy-MM-dd"
        val dailyCompletion = taskRepository.getTasksCompletedGroupedByDay(projectId, from, to)
            .mapKeys { it.key.toString() }

        val avgCompletionDays = taskRepository.avgCompletionDays(projectId)

        val overdueCount = taskRepository.findOverdue(projectId, to).size.toLong()

        return PeriodReport(
            created = created,
            completed = completed,
            statusBreakdown = statusBreakdown,
            dailyCreation = dailyCreation,
            dailyCompletion = dailyCompletion,
            averageCompletionDays = avgCompletionDays,
            overdueCount = overdueCount
        )
    }
}