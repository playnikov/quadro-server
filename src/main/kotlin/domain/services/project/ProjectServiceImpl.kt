package com.quadro.domain.services.project

import com.quadro.datasource.repositories.company.CompanyMemberRepository
import com.quadro.datasource.repositories.company.CompanyRepository
import com.quadro.datasource.repositories.project.ProjectMemberRepository
import com.quadro.datasource.repositories.project.ProjectRepository
import com.quadro.datasource.repositories.project.ProjectTeamRepository
import com.quadro.datasource.repositories.team.TeamMemberRepository
import com.quadro.datasource.repositories.team.TeamRepository
import com.quadro.datasource.repositories.users.UserRepository
import com.quadro.domain.models.company.CompanyRole
import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectCreate
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectSettings
import com.quadro.domain.models.project.ProjectStatus
import com.quadro.domain.models.project.ProjectTeam
import com.quadro.domain.models.project.ProjectUpdate
import com.quadro.domain.models.project.ProjectVisibility
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.UUID

class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val projectTeamRepository: ProjectTeamRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository
) : ProjectService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun createProject(
        userId: UUID,
        request: ProjectCreate
    ): Result<Project> {
        return try {
            val company = companyRepository.findById(request.companyId)
                ?: return Result.failure(Exception("Company not found"))

            val companyMember =
                companyMemberRepository.findByCompanyAndUser(request.companyId, userId) ?: return Result.failure(
                    Exception("User is not a member of this company")
                )

            if (!canCreateProject(companyMember.role)) {
                return Result.failure(Exception("Insufficient permissions to create project"))
            }

            if (projectRepository.existsByKey(request.companyId, request.key)) {
                return Result.failure(Exception("Project with key '${request.key}' already exists"))
            }

            if (projectRepository.existsByName(request.companyId, request.name)) {
                return Result.failure(Exception("Project with name '${request.name}' already exists"))
            }

            val now = System.currentTimeMillis()

            val project = Project(
                id = UUID.randomUUID(),
                companyId = request.companyId,
                type = request.type,
                name = request.name,
                key = request.key.uppercase(),
                description = request.description,
                status = ProjectStatus.ACTIVE,
                priority = request.priority,
                visibility = request.visibility,
                leadId = request.leadId,
                ownerId = userId,
                settings = request.settings ?: ProjectSettings(),
                startDate = request.startDate,
                endDate = request.endDate,
                completedAt = null,
                createdAt = now,
                updatedAt = now,
                archivedAt = null,
            )

            val createdProject = projectRepository.create(project)

            addProjectMember(createdProject.id, request.leadId, userId, ProjectRole.LEAD, now)

            if (userId != request.leadId) {
                addProjectMember(createdProject.id, userId, userId, ProjectRole.OWNER, now)
            }

            request.initialTeams?.forEach { assignment ->
                assignTeamToProject(
                    projectId = createdProject.id,
                    assignedBy = userId,
                    teamId = assignment.teamId,
                    role = assignment.role,
                    isLeadTeam = assignment.isLeadTeam,
                    now = now
                )
            }

            logger.info("Project created: ${createdProject.key} in company: ${request.companyId} by user: $userId")

            Result.success(createdProject)
        }  catch (e: Exception) {
            logger.error("Failed to create project", e)
            Result.failure(e)
        }
    }

    override suspend fun getProject(
        projectId: UUID,
        userId: UUID
    ): Result<Project> {
        return try {
            val project = projectRepository.findById(projectId)
                ?: return Result.failure(Exception("Project not found"))

            if (!canViewProject(project, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            Result.success(project)
        } catch (e: Exception) {
            logger.error("Failed to get project", e)
            Result.failure(e)
        }
    }

    override suspend fun getProjectByKey(
        companyId: UUID,
        key: String,
        userId: UUID
    ): Result<Project> {
        return try {
            val project = projectRepository.findByKey(companyId, key)
                ?: return Result.failure(Exception("Project not found"))

            if (!canViewProject(project, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            Result.success(project)

        } catch (e: Exception) {
            logger.error("Failed to get project by key", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProject(
        projectId: UUID,
        userId: UUID,
        request: ProjectUpdate
    ): Result<Project> {
        return try {
            val project = projectRepository.findById(projectId)
                ?: return Result.failure(Exception("Project not found"))

            val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
            if (!canEditProject(member?.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            if (request.name != null && request.name != project.name) {
                if (projectRepository.existsByName(project.companyId, request.name)) {
                    return Result.failure(Exception("Project with name '${request.name}' already exists"))
                }
            }

            val updatedProject = project.copy(
                name = request.name ?: project.name,
                description = request.description ?: project.description,
                status = request.status ?: project.status,
                priority = request.priority ?: project.priority,
                visibility = request.visibility ?: project.visibility,
                settings = request.settings?: project.settings,
                leadId = request.leadId ?: project.leadId,
                startDate = request.startDate ?: project.startDate,
                endDate = request.endDate ?: project.endDate,
                updatedAt = System.currentTimeMillis()
            )

            val savedProject = projectRepository.update(updatedProject)

            if (request.leadId != null && request.leadId != project.leadId) {
                updateProjectLead(projectId, request.leadId, userId)
            }

            logger.info("Project updated: $projectId by user: $userId")

            Result.success(savedProject)

        } catch (e: Exception) {
            logger.error("Failed to update project", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(projectId: UUID, userId: UUID): Result<Unit> {
        return try {
            projectRepository.findById(projectId)
                ?: return Result.failure(Exception("Project not found"))

            val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
            if (member?.role != ProjectRole.OWNER) {
                return Result.failure(Exception("Only project owner can delete the project"))
            }

            projectTeamRepository.removeAllByProject(projectId)
            projectMemberRepository.removeAllByProject(projectId)

            projectRepository.delete(projectId)

            logger.info("Project deleted: $projectId by user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to delete project", e)
            Result.failure(e)
        }
    }

    override suspend fun archiveProject(projectId: UUID, userId: UUID): Result<Unit> {
        return try {
            projectRepository.findById(projectId)
                ?: return Result.failure(Exception("Project not found"))

            val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
            if (!canArchiveProject(member?.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            projectRepository.updateStatus(projectId, ProjectStatus.ARCHIVED)
            logger.info("Project archived: $projectId by user: $userId")

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to archive project", e)
            Result.failure(e)
        }
    }

    override suspend fun restoreProject(projectId: UUID, userId: UUID): Result<Unit> {
        return try {
            projectRepository.findById(projectId)
                ?: return Result.failure(Exception("Project not found"))

            val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
            if (!canArchiveProject(member?.role)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            projectRepository.updateStatus(projectId, ProjectStatus.ACTIVE)
            logger.info("Project restored: $projectId by user: $userId")

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to restore project", e)
            Result.failure(e)
        }
    }

    override suspend fun getCompanyProjects(
        companyId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): Result<List<Project>> {
        return try {
            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("User is not a member of this company"))
            }

            val offset = (page - 1) * size
            val projects = projectRepository.findByCompany(companyId, size, offset)

            Result.success(projects)
        } catch (e: Exception) {
            logger.error("Failed to get company projects", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserProjects(
        userId: UUID,
        companyId: UUID?
    ): Result<List<Project>> {
        return try {
            val projects = projectRepository.findByUser(userId, companyId)
            Result.success(projects)
        } catch (e: Exception) {
            logger.error("Failed to get user projects", e)
            Result.failure(e)
        }
    }

    override suspend fun getTeamProjects(
        teamId: UUID,
        userId: UUID
    ): Result<List<Project>> {
        return try {
            teamRepository.findById(teamId)
                ?: return Result.failure(Exception("Team not found"))

            if (!teamMemberRepository.exists(teamId, userId)) {
                return Result.failure(Exception("User is not a member of this team"))
            }

            val projects = projectRepository.findByTeam(teamId)
            Result.success(projects)
        } catch (e: Exception) {
            logger.error("Failed to get team projects", e)
            Result.failure(e)
        }
    }

    override suspend fun searchProjects(
        companyId: UUID,
        userId: UUID,
        query: String
    ): Result<List<Project>> {
        return try {
            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("User is not a member of this company"))
            }

            val projects = projectRepository.search(companyId, query, 20)
            Result.success(projects)
        } catch (e: Exception) {
            logger.error("Failed to search projects", e)
            Result.failure(e)
        }
    }

    // ============== Private Methods ==============
    private suspend fun addProjectMember(
        projectId: UUID,
        targetUserId: UUID,
        invitedBy: UUID,
        role: ProjectRole,
        now: Long
    ) {
        if (projectMemberRepository.exists(projectId, targetUserId)) return

        val member = ProjectMember(
            id = UUID.randomUUID(),
            projectId = projectId,
            userId = targetUserId,
            role = role,
            joinedAt = now,
            invitedBy = invitedBy,
            invitedAt = now,
            sourceTeamId = null
        )
        projectMemberRepository.add(member)
    }

    private suspend fun assignTeamToProject(
        projectId: UUID,
        assignedBy: UUID,
        teamId: UUID,
        role: ProjectRole,
        isLeadTeam: Boolean,
        now: Long
    ): ProjectTeam {
        if (isLeadTeam) {
            val leadTeams = projectTeamRepository.findByProject(projectId)
                .filter { it.isLeadTeam }
            leadTeams.forEach {
                projectTeamRepository.updateLeadTeam(it.id, false)
            }
        }

        val projectTeam = ProjectTeam(
            id = UUID.randomUUID(),
            projectId = projectId,
            teamId = teamId,
            role = role,
            isLeadTeam = isLeadTeam,
            assignedAt = now,
            assignedBy = assignedBy
        )

        return projectTeamRepository.assign(projectTeam)
    }

    private suspend fun updateProjectLead(projectId: UUID, newLeadId: UUID, updatedBy: UUID) {
        val now = System.currentTimeMillis()

        val currentLead = projectMemberRepository.findByProjectAndUser(projectId, newLeadId)
        currentLead?.let {
            if (it.role == ProjectRole.LEAD) {
                projectMemberRepository.updateRole(it.id, ProjectRole.ADMIN)
            }
        }
        val newLead = projectMemberRepository.findByProjectAndUser(projectId, newLeadId)

        if (newLead != null) {
            projectMemberRepository.updateRole(newLead.id, ProjectRole.LEAD)
        } else {
            addProjectMember(projectId, newLeadId, updatedBy, ProjectRole.LEAD, now)
        }
    }

    private fun canCreateProject(companyRole: CompanyRole): Boolean {
        return companyRole in listOf(CompanyRole.OWNER, CompanyRole.ADMIN, CompanyRole.MANAGER)
    }

    private suspend fun canViewProject(project: Project, userId: UUID): Boolean {
        return when (project.visibility) {
            ProjectVisibility.PUBLIC -> true
            ProjectVisibility.RESTRICTED -> {
                if (projectMemberRepository.exists(project.id, userId)) return true
                val userTeams = teamMemberRepository.findByUser(userId, project.companyId)
                    .map { it.teamId }
                val projectTeams = projectTeamRepository.findByProject(project.id)
                    .map { it.teamId }
                userTeams.any { it in projectTeams }
            }
            ProjectVisibility.PRIVATE -> projectMemberRepository.exists(project.id, userId)
        }
    }

    private fun canEditProject(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canArchiveProject(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canManageTeams(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canAddMembers(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canChangeRole(role: ProjectRole?): Boolean {
        return role in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
    }

    private fun canRemoveMember(userRole: ProjectRole, targetRole: ProjectRole): Boolean {
        return when (userRole) {
            ProjectRole.OWNER -> true
            ProjectRole.LEAD -> targetRole != ProjectRole.OWNER && targetRole != ProjectRole.LEAD
            ProjectRole.ADMIN -> targetRole !in listOf(ProjectRole.OWNER, ProjectRole.LEAD, ProjectRole.ADMIN)
            else -> false
        }
    }
}