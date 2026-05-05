package com.quadro.task.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskCreate
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskUpdate
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.SprintRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.repositories.team.TeamProjectRepository
import com.quadro.task.domain.repositories.team.TeamRepository
import com.quadro.task.domain.repositories.UserRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class TaskServiceImpl(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val teamProjectRepository: TeamProjectRepository
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

        if (taskCreate.assignedTeamId != null) {
            if (!validateTeamAssignment(taskCreate.assignedTeamId, taskCreate.projectId)) {
                throw DomainException.BusinessRule("Team is not assigned to the project")
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
            assignedTeamId = taskCreate.assignedTeamId,
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

        return taskRepository.create(task)
    }

    override suspend fun updateTask(id: UUID, taskUpdate: TaskUpdate): Task {
        val existingTask = taskRepository.findById(id)
            ?: throw IllegalArgumentException("Task not found with id: $id")

        val updatedTask = existingTask.copy(
            title = taskUpdate.title ?: existingTask.title,
            description = taskUpdate.description ?: existingTask.description,
            status = taskUpdate.status ?: existingTask.status,
            priority = taskUpdate.priority ?: existingTask.priority,
            assigneeId = taskUpdate.assigneeId ?: existingTask.assigneeId,
            assignedTeamId = taskUpdate.assignedTeamId ?: existingTask.assignedTeamId,
            sprintId = taskUpdate.sprintId ?: existingTask.sprintId,
            storyPoints = taskUpdate.storyPoints ?: existingTask.storyPoints,
            estimatedHours = taskUpdate.estimatedHours ?: existingTask.estimatedHours,
            loggedHours = taskUpdate.loggedHours ?: existingTask.loggedHours,
            dueDate = taskUpdate.dueDate ?: existingTask.dueDate,
            labels = taskUpdate.labels ?: existingTask.labels,
            updatedAt = Clock.System.now()
        )

        return taskRepository.update(updatedTask)
    }

    override suspend fun deleteTask(id: UUID) {
        taskRepository.delete(id)
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

    override suspend fun getTasksByTeam(teamId: UUID, projectId: UUID): List<Task> {
        return taskRepository.findByTeam(teamId, projectId)
    }

    override suspend fun getTasksByParent(parentTaskId: UUID): List<Task> {
        return taskRepository.findByParent(parentTaskId)
    }

    override suspend fun getNextTaskNumber(projectId: UUID): Int {
        return taskRepository.nextNumber(projectId)
    }

    override suspend fun countTasksByProject(projectId: UUID): Long {
        return taskRepository.countByProject(projectId)
    }

    override suspend fun countTasksByStatus(projectId: UUID, status: TaskStatus): Long {
        return taskRepository.countByStatus(projectId, status)
    }

    override suspend fun countTasksByStatusAndPeriod(
        projectId: UUID,
        status: TaskStatus,
        from: Instant,
        to: Instant
    ): Long {
        return taskRepository.countByStatusAndPeriod(projectId, status, from, to)
    }

    override suspend fun findOverdueTasks(projectId: UUID, now: Instant): List<Task> {
        return taskRepository.findOverdue(projectId, now)
    }

    override suspend fun getAverageCompletionDays(projectId: UUID): Double {
        return taskRepository.avgCompletionDays(projectId)
    }

    private suspend fun validateUserAssignment(projectId: UUID, userId: UUID): Boolean =
        projectMemberRepository.findByProjectAndUser(projectId, userId) != null

    private suspend fun validateTeamAssignment(teamId: UUID, projectId: UUID): Boolean =
        teamProjectRepository.findByTeamAndProject(teamId, projectId) != null
}