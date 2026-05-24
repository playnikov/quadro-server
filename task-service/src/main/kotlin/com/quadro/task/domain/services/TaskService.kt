package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskCreate
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskUpdate
import java.util.UUID
import kotlin.time.Instant

interface TaskService {
    suspend fun createTask(taskCreate: TaskCreate, reporterId: UUID): Task
    suspend fun updateTask(id: UUID, taskUpdate: TaskUpdate): Task
    suspend fun deleteTask(id: UUID)
    suspend fun getTask(id: UUID): Task?
    suspend fun getTasksByProject(projectId: UUID, limit: Int, offset: Int): List<Task>
    suspend fun getTasksBySprint(sprintId: UUID): List<Task>
    suspend fun getTasksByAssignee(userId: UUID): List<Task>
    suspend fun getTasksByParent(parentTaskId: UUID): List<Task>
    suspend fun getNextTaskNumber(projectId: UUID): Int
    suspend fun countTasksByProject(projectId: UUID): Long
    suspend fun countTasksByStatus(projectId: UUID, status: TaskStatus): Long
    suspend fun countTasksByStatusAndPeriod(
        projectId: UUID,
        status: TaskStatus,
        from: Instant,
        to: Instant
    ): Long
    suspend fun findOverdueTasks(projectId: UUID, now: Instant): List<Task>
    suspend fun getAverageCompletionDays(projectId: UUID): Double
}