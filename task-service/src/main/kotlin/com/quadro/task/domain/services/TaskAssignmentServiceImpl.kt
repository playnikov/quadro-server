package com.quadro.task.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.repositories.team.TeamProjectRepository
import com.quadro.task.domain.repositories.team.TeamRepository
import com.quadro.task.domain.repositories.UserRepository
import java.util.UUID
import kotlin.time.Clock

class TaskAssignmentServiceImpl(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val teamProjectRepository: TeamProjectRepository
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
            assignedTeamId = null,
            updatedAt = Clock.System.now()
        )

        return taskRepository.update(updatedTask)
    }

    override suspend fun assignTaskToTeam(taskId: UUID, teamId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found with id: $taskId")

        val team = teamRepository.findById(teamId)
            ?: throw DomainException.NotFound("Team", teamId.toString())

        if (!validateTeamAssignment(taskId, teamId)) {
            throw DomainException.BusinessRule("Team is not assigned to the project")
        }

        val updatedTask = task.copy(
            assigneeId = null,
            assignedTeamId = teamId,
            updatedAt = Clock.System.now()
        )

        return taskRepository.update(updatedTask)
    }

    override suspend fun unassignTask(taskId: UUID): Task {
        val task = taskRepository.findById(taskId)
            ?: throw IllegalArgumentException("Task not found with id: $taskId")

        val updatedTask = task.copy(
            assigneeId = null,
            assignedTeamId = null,
            updatedAt = Clock.System.now()
        )

        return taskRepository.update(updatedTask)
    }

    override suspend fun validateUserAssignment(taskId: UUID, userId: UUID): Boolean {
        val task = taskRepository.findById(taskId)
            ?: return false

        val user = userRepository.findById(userId) ?: return false

        val project = projectRepository.findById(task.projectId) ?: return false

        // Проверка членства пользователя в проекте
        val projectMember = projectMemberRepository.findByProjectAndUser(task.projectId, userId)
        return projectMember != null
    }

    override suspend fun validateTeamAssignment(taskId: UUID, teamId: UUID): Boolean {
        val task = taskRepository.findById(taskId)
            ?: return false

        // Проверка существования команды
        val team = teamRepository.findById(teamId) ?: return false

        // Проверка существования проекта
        val project = projectRepository.findById(task.projectId) ?: return false

        // Проверка назначения команды проекту
        val teamProject = teamProjectRepository.findByTeamAndProject(teamId, task.projectId)
        return teamProject != null
    }
}