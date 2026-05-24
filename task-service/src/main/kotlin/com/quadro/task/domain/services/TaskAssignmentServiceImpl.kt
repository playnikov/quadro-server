package com.quadro.task.domain.services

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TaskAssignedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.repositories.UserRepository
import java.util.UUID
import kotlin.time.Clock

class TaskAssignmentServiceImpl(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val eventProducer: EventProducer
) : TaskAssignmentService {

    override suspend fun assignTaskToUser(taskId: UUID, userId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw DomainException.NotFound("Task", taskId.toString())

        userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())

        if (!validateUserAssignment(taskId, userId)) {
            throw DomainException.BusinessRule("User is not a member of the project team")
        }

        val updatedTask = task.copy(
            assigneeId = userId,
            updatedAt = Clock.System.now()
        )

        taskRepository.update(updatedTask)

        eventProducer.publish(
            topic = KafkaTopics.TASK_ASSIGNED,
            key = updatedTask.id.toString(),
            event = TaskAssignedEvent(
                taskId = updatedTask.id.toString(),
                projectId = updatedTask.projectId.toString(),
                assigneeId = updatedTask.assigneeId.toString()
            )
        )

        return updatedTask
    }

    override suspend fun unassignTask(taskId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found with id: $taskId")

        val updatedTask = task.copy(
            assigneeId = null,
            updatedAt = Clock.System.now()
        )

        return taskRepository.update(updatedTask)
    }

    override suspend fun validateUserAssignment(taskId: UUID, userId: UUID): Boolean {
        val task = taskRepository.findById(taskId)
            ?: return false

        val user = userRepository.findById(userId) ?: return false

        val project = projectRepository.findById(task.projectId) ?: return false

        val projectMember = projectMemberRepository.findByProjectAndUser(task.projectId, userId)
        return projectMember != null
    }
}