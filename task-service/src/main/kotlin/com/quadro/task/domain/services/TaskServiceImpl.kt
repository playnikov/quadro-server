package com.quadro.task.domain.services

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TaskCreatedEvent
import com.quadro.shared.data.messaging.events.TaskDeletedEvent
import com.quadro.shared.data.messaging.events.TaskUpdatedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskCreate
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskUpdate
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val taskHistoryService: TaskHistoryService,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val eventProducer: EventProducer
) : TaskService {

    override suspend fun createTask(taskCreate: TaskCreate, reporterId: UUID): Task {
        taskCreate.validate()

        val project = projectRepository.findById(taskCreate.projectId)
            ?: throw DomainException.NotFound("Project", taskCreate.projectId.toString())

        if (taskCreate.assigneeId != null) {
            if (!validateUserAssignment(taskCreate.projectId, taskCreate.assigneeId)) {
                throw DomainException.BusinessRule("User is not a member of the project team")
            }
        }

        val now = Clock.System.now()
        val nextNumber = taskRepository.nextNumber(project.id)

        val task = Task(
            id = UUID.randomUUID(),
            projectId = project.id,
            sprintId = taskCreate.sprintId,
            parentTaskId = taskCreate.parentTaskId,
            number = nextNumber,
            title = taskCreate.title,
            description = taskCreate.description,
            status = TaskStatus.BACKLOG,
            priority = taskCreate.priority,
            type = taskCreate.type,
            assigneeId = taskCreate.assigneeId,
            reporterId = reporterId,
            storyPoints = taskCreate.storyPoints,
            estimatedHours = taskCreate.estimatedHours,
            loggedHours = 0.0,
            dueDate = taskCreate.dueDate,
            startedAt = null,
            completedAt = null,
            createdAt = now,
            updatedAt = now,
            labels = taskCreate.labels
        )

        eventProducer.publish(
            topic = KafkaTopics.TASK_CREATED,
            key = task.id.toString(),
            event = TaskCreatedEvent(
                taskId = task.id.toString(),
                projectId = task.projectId.toString(),
                title = task.title,
                description = task.description,
                status = task.status.name,
                priority = task.priority.name,
                type = task.type.name,
                assigneeId = task.assigneeId.toString()
            )
        )

        val result = taskRepository.create(task)

        taskHistoryService.recordTaskCreate(result, reporterId)
        return result
    }

    override suspend fun updateTask(requesterId: UUID, id: UUID, taskUpdate: TaskUpdate): Task {
        val existingTask = taskRepository.findById(id)
            ?: throw IllegalArgumentException("Task not found with id: $id")

        val now = Clock.System.now()
        val updatedTask = existingTask.copy(
            title = taskUpdate.title ?: existingTask.title,
            description = taskUpdate.description ?: existingTask.description,
            status = taskUpdate.status?.let {
                taskHistoryService.recordStatusChange(id, requesterId, existingTask.status.name, it.name)
                it
            } ?: existingTask.status,
            priority = taskUpdate.priority ?: existingTask.priority,
            assigneeId = taskUpdate.assigneeId?.let {
                taskHistoryService.recordAssigneeChange(id, requesterId, existingTask.assigneeId, it)
                it
            } ?: existingTask.assigneeId,
            sprintId = taskUpdate.sprintId ?: existingTask.sprintId,
            storyPoints = taskUpdate.storyPoints ?: existingTask.storyPoints,
            estimatedHours = taskUpdate.estimatedHours ?: existingTask.estimatedHours,
            loggedHours = taskUpdate.loggedHours ?: existingTask.loggedHours,
            dueDate = taskUpdate.dueDate ?: existingTask.dueDate,
            labels = taskUpdate.labels ?: existingTask.labels,
            completedAt = taskUpdate.status?.let {
                if (it == TaskStatus.DONE) {
                    now
                } else {
                    null
                }
            },
            updatedAt = now
        )

        val updatedTaskWithReporter = taskRepository.update(updatedTask)
        eventProducer.publish(
            topic = KafkaTopics.TASK_UPDATED,
            key = updatedTaskWithReporter.id.toString(),
            event = TaskUpdatedEvent(
                taskId = updatedTaskWithReporter.id.toString(),
                projectId = updatedTaskWithReporter.projectId.toString(),
                title = updatedTaskWithReporter.title,
                description = updatedTaskWithReporter.description,
                status = updatedTaskWithReporter.status.name,
                priority = updatedTaskWithReporter.priority.name,
                assigneeId = updatedTaskWithReporter.assigneeId?.toString(),
                updatedAt = updatedTaskWithReporter.updatedAt.toEpochMilliseconds(),
                updatedBy = requesterId.toString()
            )
        )
        return updatedTaskWithReporter
    }

    override suspend fun deleteTask(id: UUID) {
        val task = taskRepository.findById(id)
            ?: throw DomainException.NotFound("Task", id.toString())
        taskRepository.delete(id)

        eventProducer.publish(
            topic = KafkaTopics.TASK_DELETED,
            key = id.toString(),
            event = TaskDeletedEvent(
                taskId = task.id.toString(),
                projectId = task.projectId.toString()
            )
        )
    }

    override suspend fun getTask(id: UUID): Task? {
        return taskRepository.findById(id)
    }

    override suspend fun getTasksByProject(projectId: UUID, limit: Int, offset: Int): List<Task> {
        return taskRepository.findByProject(projectId, limit, offset)
    }

    override suspend fun getTasksBySprint(sprintId: UUID): List<Task> {
        return taskRepository.findBySprint(sprintId)
    }

    override suspend fun getTasksByAssignee(userId: UUID): List<Task> {
        return taskRepository.findByAssignee(userId)
    }

    override suspend fun getTasksByParent(parentTaskId: UUID): List<Task> {
        return taskRepository.findByParent(parentTaskId)
    }

    override suspend fun getNextTaskNumber(projectId: UUID): Int {
        return taskRepository.nextNumber(projectId)
    }

    private suspend fun validateUserAssignment(projectId: UUID, userId: UUID): Boolean =
        projectMemberRepository.findByProjectAndUser(projectId, userId) != null
}