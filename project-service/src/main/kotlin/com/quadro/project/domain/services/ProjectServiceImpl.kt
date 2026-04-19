package com.quadro.project.domain.services

import com.quadro.project.domain.models.Company
import com.quadro.project.domain.models.CompanyRole
import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectCreate
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectUpdate
import com.quadro.project.domain.models.User
import com.quadro.project.domain.repositories.CompanyMemberRepository
import com.quadro.project.domain.repositories.CompanyRepository
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectArchivedEvent
import com.quadro.shared.dto.DomainException
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val companyRepository: CompanyRepository,
    private val userRepository: UserRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val eventProducer: EventProducer
) : ProjectService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun ensureUserCanManageProjects(
        userId: UUID,
        companyId: UUID,
        action: String
    ): Pair<User, Company> {
        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "ID: $userId")

        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", "ID: $companyId")

        if (!company.isActive())
            throw DomainException.AccessDenied("Company is not active")

        val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("User is not a member of the company")

        if (!member.role.isAtLeast(company.projectManagementRole)) {
            logger.warn("User $userId (role: ${member.role}) denied project management access for $action")
            throw DomainException.AccessDenied("Insufficient permissions to $action projects")
        }

        return user to company
    }

    override suspend fun createProject(
        userId: UUID,
        request: ProjectCreate
    ): Project {
        val (user, company) = ensureUserCanManageProjects(userId, request.companyId, "create")

        if (company.currentProjects >= company.maxProjects) {
            throw DomainException.AlreadyExists("Maximum number of active Projects reached for the current plan.")
        }

        request.validate()
        if (projectRepository.existsByName(request.companyId, request.name)) {
            logger.warn("User ${userId} attempted to create project with existing name: ${request.name} in company: ${request.companyId}")
            throw DomainException.AlreadyExists("Project with name ${request.name} already exists")
        }
        if (projectRepository.existsByKey(request.companyId, request.key)) {
            logger.warn("User ${userId} attempted to create project with existing key: ${request.key} in company: ${request.companyId}")
            throw DomainException.AlreadyExists("Project with key ${request.key} already exists")
        }

        val now = Clock.System.now()
        val project = Project(
            id = UUID.randomUUID(),
            companyId = request.companyId,
            type = request.type,
            name = request.name,
            key = request.key,
            description = request.description,
            status = ProjectStatus.ACTIVE,
            priority = request.priority,
            visibility = request.visibility,
            leadId = request.leadId,
            ownerId = userId,
            startDate = request.startDate,
            endDate = request.endDate,
            completedAt = null,
            createdAt = now,
            updatedAt = now
        )

        val createdProject = projectRepository.create(project)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_CREATED,
            key = createdProject.id.toString(),
            event = ProjectCreatedEvent(
                projectId = createdProject.id.toString(),
                companyId = createdProject.companyId.toString(),
                name = createdProject.name,
                status = createdProject.status.name
            )
        )

        companyRepository.incrementProjectCount(createdProject.companyId)
        logger.info("Created Project ${createdProject.name} (ID: ${createdProject.id}) by user: $userId")
        return createdProject
    }

    override suspend fun updateProject(
        userId: UUID,
        projectId: UUID,
        request: ProjectUpdate
    ): Project {
        ensureUserCanManageProjects(userId, request.companyId, "update")

        request.leadId ?.let { userRepository.findById(request.leadId)
            ?: throw DomainException.NotFound("User", "User Not Found") }

        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")

        if (project.completedAt != null) {
            throw DomainException.AccessDenied("Project '${projectId}' completed")
        }

        if (request.name != null && request.name != project.name && projectRepository.existsByName(request.companyId, request.name)) {
            logger.warn("User ${userId} attempted to rename project ${projectId} to existing name: ${request.name}")
            throw DomainException.AlreadyExists("Project with name ${request.name} already exists")
        }

        val now = Clock.System.now()
        val updateProject = project.copy(
            name = request.name ?: project.name,
            description = request.description ?: project.description,
            status = request.status ?: project.status,
            priority = request.priority ?: project.priority,
            visibility = request.visibility ?: project.visibility,
            leadId = request.leadId ?: project.leadId,
            startDate = request.startDate ?: project.startDate,
            endDate = request.endDate ?: project.endDate,
            updatedAt = now
        )

        val saved = projectRepository.update(updateProject)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_UPDATED,
            key = saved.id.toString(),
            event = ProjectUpdatedEvent(
                projectId = saved.id.toString(),
                companyId = saved.companyId.toString(),
                name = saved.name,
                status = saved.status.name
            )
        )

        logger.info("Updated Project ${saved.name} (ID: ${saved.id}) by user: $userId")
        return saved
    }

    override suspend fun deleteProject(userId: UUID, projectId: UUID) {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")
        val (user, company) = ensureUserCanManageProjects(userId, project.companyId, "delete")

        projectRepository.delete(projectId)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_DELETED,
            key = project.id.toString(),
            event = ProjectDeletedEvent(
                projectId = project.id.toString(),
                companyId = project.companyId.toString(),
                deletedBy = userId.toString()
            )
        )

        companyRepository.decrementProjectCount(project.companyId)
        logger.info("Deleted Project ${project.name} (ID: ${project.id}) by user: $userId")
    }

    override suspend fun findById(projectId: UUID): Project {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", projectId.toString())
        return project
    }

    override suspend fun findByName(
        companyId: UUID,
        name: String
    ): Project {
        companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", "Company Not Found")
        val project = projectRepository.findByName(companyId, name)
            ?: throw DomainException.NotFound("Project", name)
        return project
    }

    override suspend fun findByKey(
        companyId: UUID,
        key: String
    ): Project {
        companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", "Company Not Found")
        val project = projectRepository.findByKey(companyId, key)
            ?: throw DomainException.NotFound("Project", "Project Not Found")
        return project
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?,
        limit: Int,
        offset: Int
    ): List<Project> {
        userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "User Not Found")

        val projects = projectRepository.findByUser(userId, companyId, limit, offset)

        logger.info("User ${userId} requested list of projects, company: ${companyId}, limit: ${limit}, offset: ${offset}, count: ${projects.size}")
        return projects
    }

    override suspend fun findByCompany(
        companyId: UUID,
        limit: Int,
        offset: Int
    ): List<Project> {
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", "Company Not Found")

        val projects = projectRepository.findByCompany(companyId, limit, offset)
        logger.info("User requested projects for company ${company.name} (ID: ${companyId}), limit: ${limit}, offset: ${offset}, count: ${projects.size}")
        return projects
    }

    override suspend fun updateStatus(
        userId: UUID,
        projectId: UUID,
        status: ProjectStatus
    ): Boolean {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")

        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "User Not Found")

        val member = companyMemberRepository.findByCompanyAndUser(project.companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")

        if (member.role !in listOf(CompanyRole.OWNER, CompanyRole.ADMIN)) {
            logger.warn("User ${userId} with role ${member.role} attempted to update project status ${projectId} without sufficient permissions")
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val result = projectRepository.updateStatus(projectId, status)

        if (result && status == ProjectStatus.ARCHIVED) {
            val project = projectRepository.findById(projectId)
            if (project != null) {
                eventProducer.publish(
                    topic = KafkaTopics.PROJECT_ARCHIVED,
                    key = project.id.toString(),
                    event = ProjectArchivedEvent(
                        projectId = project.id.toString(),
                        companyId = project.companyId.toString(),
                        archivedBy = userId.toString()
                    )
                )
                companyRepository.decrementProjectCount(project.companyId)
                logger.info("Project ${project.name} (ID: ${project.id}) archived by user: $userId")
            }
        } else if (result) {
            logger.info("Project ${project.name} (ID: ${project.id}) status updated to $status by user: $userId")
        }

        return result
    }
}