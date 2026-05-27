package com.quadro.task.domain.services

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TaskAssignedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.project.MemberRole
import com.quadro.task.domain.models.project.ProjectMember
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.repositories.UserRepository
import java.util.UUID
import kotlin.time.Clock

class TaskAssignmentServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskHistoryService: TaskHistoryService,
    private val projectMemberRepository: ProjectMemberRepository,
    private val eventProducer: EventProducer
) : TaskAssignmentService {
    private suspend fun checkProjectManagePermission(projectId: UUID, userId: UUID): ProjectMember {
        val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
        if (member == null || !member.role.isAtLeast(MemberRole.MANAGER)) {
            throw DomainException.AccessDenied("Insufficient permissions: need OWNER or MANAGER")
        }
        return member
    }

    override suspend fun assignTaskToUser(taskId: UUID, userId: UUID, requestId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw DomainException.NotFound("Task", taskId.toString())

        if (task.assigneeId != null || userId != requestId) {
            checkProjectManagePermission(task.projectId, requestId)
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
                title = updatedTask.title,
                assigneeId = updatedTask.assigneeId.toString()
            )
        )


        taskHistoryService.recordAssigneeChange(
            taskId = task.id,
            userId = requestId,
            oldAssignee = task.assigneeId,
            newAssignee = updatedTask.assigneeId
        )
        return updatedTask
    }

    override suspend fun unassignTask(taskId: UUID, requestId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found with id: $taskId")

        checkProjectManagePermission(task.projectId, requestId)

        val updatedTask = task.copy(
            assigneeId = null,
            updatedAt = Clock.System.now()
        )

        taskHistoryService.recordAssigneeChange(
            taskId = task.id,
            userId = requestId,
            oldAssignee = task.assigneeId,
            newAssignee = updatedTask.assigneeId
        )
        return taskRepository.update(updatedTask)
    }
}