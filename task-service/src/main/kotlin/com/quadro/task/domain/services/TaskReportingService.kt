package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.PeriodReport
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import java.util.UUID
import kotlin.time.Instant

interface TaskReportingService {
    suspend fun getBacklogCount(projectId: UUID): Long
    suspend fun getTodoCount(projectId: UUID): Long
    suspend fun getInProgressCount(projectId: UUID): Long
    suspend fun getInReviewCount(projectId: UUID): Long
    suspend fun getDoneCount(projectId: UUID): Long
    suspend fun getCancelledCount(projectId: UUID): Long
    suspend fun getTaskCounts(projectId: UUID): Map<TaskStatus, Long>
    suspend fun getOverdueTasks(projectId: UUID, now: Instant): List<Task>
    suspend fun getAverageCompletionDays(projectId: UUID): Double
    suspend fun getCompletionRate(projectId: UUID): Double
    suspend fun getVelocity(projectId: UUID): Double
    suspend fun getPeriodReport(projectId: UUID, from: Instant, to: Instant): PeriodReport
}