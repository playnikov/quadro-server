package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.PeriodReport
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.repositories.UserRepository
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.quadro.shared.utils.toOffsetDateTime
import com.quadro.task.domain.models.task.DurationPercentiles
import com.quadro.task.domain.models.task.VelocityMetric
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class TaskReportingServiceImpl(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository
) : TaskReportingService {
    private val reportCache = Caffeine.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .maximumSize(100)
        .build<CacheKey, PeriodReport>()

    private data class CacheKey(
        val projectId: UUID,
        val from: Instant,
        val to: Instant
    )

    override suspend fun getBacklogCount(projectId: UUID): Long = taskRepository.countByStatus(projectId, TaskStatus.BACKLOG)
    override suspend fun getTodoCount(projectId: UUID): Long = taskRepository.countByStatus(projectId, TaskStatus.TODO)
    override suspend fun getInProgressCount(projectId: UUID): Long = taskRepository.countByStatus(projectId, TaskStatus.IN_PROGRESS)
    override suspend fun getInReviewCount(projectId: UUID): Long = taskRepository.countByStatus(projectId, TaskStatus.IN_REVIEW)
    override suspend fun getDoneCount(projectId: UUID): Long = taskRepository.countByStatus(projectId, TaskStatus.DONE)
    override suspend fun getCancelledCount(projectId: UUID): Long = taskRepository.countByStatus(projectId, TaskStatus.CANCELLED)

    override suspend fun getTaskCounts(projectId: UUID): Map<TaskStatus, Long> {
        val counts = taskRepository.countGroupedByStatus(projectId)
        return TaskStatus.entries.associateWith { counts[it] ?: 0L }
    }

    override suspend fun getOverdueTasks(projectId: UUID, now: Instant, page: Int, size: Int): List<Task> =
        taskRepository.findOverduePaginated(projectId, now, page, size)

    override suspend fun getOverdueCount(projectId: UUID, now: Instant): Long =
        taskRepository.countOverdue(projectId, now)

    override suspend fun getAverageCompletionDays(projectId: UUID, from: Instant?, to: Instant?): Double =
        taskRepository.avgCompletionDaysInPeriod(projectId, from, to)

    override suspend fun getCompletionRate(projectId: UUID): Double {
        val total = taskRepository.countByProject(projectId)
        if (total == 0L) return 0.0
        val completed = taskRepository.countByStatus(projectId, TaskStatus.DONE)
        return (completed.toDouble() / total) * 100
    }

    override suspend fun getVelocity(projectId: UUID, metric: VelocityMetric, daysBack: Long): Double {
        val now = Clock.System.now()
        val from = now - daysBack.days
        return when (metric) {
            VelocityMetric.TASK_COUNT -> taskRepository.countCompletedByPeriod(projectId, from, now).toDouble()
            VelocityMetric.STORY_POINTS -> taskRepository.sumStoryPointsCompletedInPeriod(projectId, from, now) ?: 0.0
            VelocityMetric.ESTIMATED_HOURS -> taskRepository.sumEstimatedHoursCompletedInPeriod(projectId, from, now) ?: 0.0
        }
    }

    override suspend fun getPeriodReport(
        projectId: UUID,
        from: Instant,
        to: Instant
    ): PeriodReport {
        val daysInPeriod = ChronoUnit.DAYS.between(from.toOffsetDateTime(), to.toOffsetDateTime()) + 1

        val created = taskRepository.countCreatedByPeriod(projectId, from, to)
        val completed = taskRepository.countCompletedByPeriod(projectId, from, to)

        val statusBreakdown = TaskStatus.entries.associate { status ->
            status.name to taskRepository.countByStatusAndPeriod(projectId, status, from, to)
        }

        val dailyCreation = taskRepository.getTasksCreatedGroupedByDay(projectId, from, to)
            .mapKeys { it.key.toString() } // LocalDate -> "yyyy-MM-dd"
        val dailyCompletion = taskRepository.getTasksCompletedGroupedByDay(projectId, from, to)
            .mapKeys { it.key.toString() }

        val overdueCount = taskRepository.countOverdue(projectId, to)
        val avgCompletionDays = taskRepository.avgCompletionDaysInPeriod(projectId, from, to)
        val throughput = if (daysInPeriod > 0) completed.toDouble() / daysInPeriod else 0.0
        val efficiency = if (created > 0) (completed.toDouble() / created) * 100 else 0.0
        val wipAverage = taskRepository.averageWipInPeriod(projectId, from, to)

        return PeriodReport(
            created = created,
            completed = completed,
            statusBreakdown = statusBreakdown,
            dailyCreation = dailyCreation,
            dailyCompletion = dailyCompletion,
            averageCompletionDays = avgCompletionDays,
            overdueCount = overdueCount,
            throughput = throughput,
            efficiency = efficiency,
            wipAverage = wipAverage
        )
    }

}