package com.quadro.task.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.repositories.task.TaskRepository
import java.util.UUID
import kotlin.time.Clock

class TaskStatusServiceImpl(
    private val taskRepository: TaskRepository
) : TaskStatusService {

    override suspend fun transitionStatus(taskId: UUID, newStatus: TaskStatus): Task {
        val task = taskRepository.findById(taskId)
            ?: throw DomainException.NotFound("Task", taskId.toString())

        if (!validateStatusTransition(taskId, newStatus)) {
            throw DomainException.InvalidTransition(task.status, newStatus)
        }

        val now = Clock.System.now()
        val updatedTask = task.copy(
            status = newStatus,
            startedAt = when {
                task.status != TaskStatus.IN_PROGRESS && newStatus == TaskStatus.IN_PROGRESS -> now
                else -> task.startedAt
            },
            completedAt = when {
                task.status != TaskStatus.DONE && newStatus == TaskStatus.DONE -> now
                else -> task.completedAt
            },
            updatedAt = now
        )

        return taskRepository.update(updatedTask)
    }

    override suspend fun validateStatusTransition(taskId: UUID, newStatus: TaskStatus): Boolean {
        val task = taskRepository.findById(taskId) ?: return false
        return task.status.canTransitionTo(newStatus)
    }

    override suspend fun startTask(taskId: UUID): Task {
        return transitionStatus(taskId, TaskStatus.IN_PROGRESS)
    }

    override suspend fun completeTask(taskId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found with id: $taskId")

        if (task.estimatedHours == null) {
            throw DomainException.BusinessRule("Cannot complete task without estimated hours")
        }

        return transitionStatus(taskId, TaskStatus.DONE)
    }

    override suspend fun cancelTask(taskId: UUID): Task {
        return transitionStatus(taskId, TaskStatus.CANCELLED)
    }

    override suspend fun reopenTask(taskId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found with id: $taskId")

        return when (task.status) {
            TaskStatus.DONE -> transitionStatus(taskId, TaskStatus.IN_PROGRESS)
            TaskStatus.CANCELLED -> transitionStatus(taskId, TaskStatus.BACKLOG)
            else -> throw DomainException.BusinessRule("Task can only be reopened from DONE or CANCELLED status")
        }
    }
}