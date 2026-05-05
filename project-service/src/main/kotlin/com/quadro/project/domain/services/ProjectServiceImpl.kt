package com.quadro.project.domain.services

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectCreate
import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.ProjectMemberResponse
import com.quadro.project.domain.models.ProjectRole
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectUpdate
import com.quadro.project.domain.models.User
import com.quadro.project.domain.models.UserRole
import com.quadro.project.domain.repositories.ProjectMemberRepository
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectArchivedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberAddedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberRemovedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberUpdatedRoleEvent
import com.quadro.shared.dto.DomainException
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class ProjectServiceImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val eventProducer: EventProducer
) : ProjectService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun existsUser(userId: UUID): User =
        userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "ID: $userId")

    private suspend fun checkProjectAccess(projectId: UUID, userId: UUID, requiredRole: ProjectRole): ProjectMember {
        val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
            ?: throw DomainException.AccessDenied("User is not a member of the project")

        if (member.role !in listOf(ProjectRole.OWNER, ProjectRole.ADMIN) && 
            userRepository.findById(userId)?.role == UserRole.ADMIN) {
            return member.copy(role = ProjectRole.ADMIN)
        }

        if (member.role !in listOf(ProjectRole.OWNER, ProjectRole.ADMIN) && 
            userRepository.findById(userId)?.role == UserRole.SUPER_ADMIN) {
            return member.copy(role = ProjectRole.OWNER)
        }

        if (member.role < requiredRole) {
            throw DomainException.AccessDenied("Insufficient permissions: ${member.role} < $requiredRole")
        }
        
        return member
    }

    override suspend fun createProject(
        userId: UUID,
        request: ProjectCreate
    ): Project {
        existsUser(userId)
        request.validate()

        if (projectRepository.existsByKey(request.key)) {
            logger.warn("User $userId attempted to create project with existing key: ${request.key}")
            throw DomainException.AlreadyExists("Project with key ${request.key} already exists")
        }

        val now = Clock.System.now()
        val project = Project(
            id = UUID.randomUUID(),
            type = request.type,
            name = request.name,
            key = request.key,
            description = request.description,
            status = ProjectStatus.ACTIVE,
            priority = request.priority,
            visibility = request.visibility,
            startDate = request.startDate,
            endDate = request.endDate,
            completedAt = null,
            createdAt = now,
            updatedAt = now
        )

        val createdProject = projectRepository.create(project)

        val member = ProjectMember(
            id = UUID.randomUUID(),
            projectId = createdProject.id,
            userId = userId,
            role = ProjectRole.OWNER,
            joinedAt = now,
            invitedBy = userId,
            invitedAt = now
        )
        projectMemberRepository.add(member)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_CREATED,
            key = createdProject.id.toString(),
            event = ProjectCreatedEvent(
                projectId = createdProject.id.toString(),
                ownerId = userId.toString(),
                name = createdProject.name,
                status = createdProject.status.name,
                key = createdProject.key
            )
        )

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_MEMBER_ADDED,
            key = member.id.toString(),
            event = ProjectMemberAddedEvent(
                projectId = member.projectId.toString(),
                userId = member.userId.toString(),
                role = member.role.name
            )
        )

        logger.info("Created Project ${createdProject.name} (ID: ${createdProject.id}) by user: $userId")
        return createdProject
    }

    override suspend fun updateProject(
        userId: UUID,
        projectId: UUID,
        request: ProjectUpdate
    ): Project {
        existsUser(userId)

        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")

        checkProjectAccess(projectId, userId, ProjectRole.MANAGER)

        if (project.completedAt != null) {
            throw DomainException.AccessDenied("Project '${projectId}' completed")
        }

        if (request.name != null && request.name != project.name && projectRepository.existsByName(request.name)) {
            logger.warn("User $userId attempted to rename project $projectId to existing name: ${request.name}")
            throw DomainException.AlreadyExists("Project with name ${request.name} already exists")
        }

        val now = Clock.System.now()
        val updateProject = project.copy(
            name = request.name ?: project.name,
            description = request.description ?: project.description,
            status = request.status ?: project.status,
            priority = request.priority ?: project.priority,
            visibility = request.visibility ?: project.visibility,
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
                ownerId = userId.toString(),
                name = saved.name,
                status = saved.status.name,
                key = saved.key
            )
        )

        logger.info("Updated Project ${saved.name} (ID: ${saved.id}) by user: $userId")
        return saved
    }

    override suspend fun deleteProject(userId: UUID, projectId: UUID) {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")

        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "User Not Found")
        
        if (user.role !in listOf(UserRole.SUPER_ADMIN, UserRole.ADMIN)) {
            checkProjectAccess(projectId, userId, ProjectRole.OWNER)
        }

        projectRepository.delete(projectId)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_DELETED,
            key = project.id.toString(),
            event = ProjectDeletedEvent(
                projectId = project.id.toString(),
                deletedBy = userId.toString()
            )
        )

        logger.info("Deleted Project ${project.name} (ID: ${project.id}) by user: $userId")
    }

    override suspend fun findById(projectId: UUID): Project {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", projectId.toString())
        return project
    }

    override suspend fun findByName(name: String): Project {
        val project = projectRepository.findByName(name)
            ?: throw DomainException.NotFound("Project", name)
        return project
    }

    override suspend fun findByKey(key: String): Project {
        val project = projectRepository.findByKey(key)
            ?: throw DomainException.NotFound("Project", key)
        return project
    }

    override suspend fun findByUser(
        userId: UUID,
        limit: Int,
        offset: Int
    ): List<Project> {
        userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "User Not Found")

        val projects = projectRepository.findByUser(userId, limit, offset)

        logger.info("User $userId requested list of projects, limit: ${limit}, offset: ${offset}, count: ${projects.size}")
        return projects
    }

    override suspend fun updateStatus(
        userId: UUID,
        projectId: UUID,
        status: ProjectStatus
    ): Boolean {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")

        checkProjectAccess(projectId, userId, ProjectRole.MANAGER)

        val result = projectRepository.updateStatus(projectId, status)

        if (result && status == ProjectStatus.ARCHIVED) {
            val project = projectRepository.findById(projectId)
            if (project != null) {
                eventProducer.publish(
                    topic = KafkaTopics.PROJECT_ARCHIVED,
                    key = project.id.toString(),
                    event = ProjectArchivedEvent(
                        projectId = project.id.toString(),
                        archivedBy = userId.toString()
                    )
                )
                logger.info("Project ${project.name} (ID: ${project.id}) archived by user: $userId")
            }
        } else if (result) {
            logger.info("Project ${project.name} (ID: ${project.id}) status updated to $status by user: $userId")
        }

        return result
    }

    override suspend fun getProjectMembers(
        projectId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): List<ProjectMemberResponse> {
        val offset = (page - 1) * size
        val members = projectMemberRepository.findByProject(projectId, size, offset)

        logger.info("User $userId requested members of project ${projectId}, page: $page")
        return members.map { member ->
            ProjectMemberResponse.from(member)
        }
    }

    override suspend fun updateMemberRole(
        projectId: UUID,
        userId: UUID,
        targetUserId: UUID,
        role: ProjectRole
    ) {
        if (userId == targetUserId) {
            logger.warn("User $userId attempted to change their own role in project $projectId")
            throw DomainException.Forbidden("Not allowed")
        }
        
        val requestingMember = checkProjectAccess(projectId, userId, ProjectRole.MANAGER)
        
        if (requestingMember.role == ProjectRole.MANAGER && role in listOf(ProjectRole.ADMIN, ProjectRole.OWNER)) {
            throw DomainException.AccessDenied("Manager cannot grant ADMIN or OWNER roles")
        }
        
        val targetMember = projectMemberRepository.findByProjectAndUser(projectId, targetUserId)
            ?: throw DomainException.NotFound("Target user is not a member of the project", targetUserId.toString())
            
        if (targetMember.role == ProjectRole.OWNER && role != ProjectRole.OWNER) {
            if (requestingMember.role != ProjectRole.OWNER) {
                throw DomainException.AccessDenied("Only OWNER can modify another OWNER's role")
            }

            val ownerCount = projectMemberRepository.findByProjectAndRole(projectId, ProjectRole.OWNER).size
            if (ownerCount <= 1) {
                throw DomainException.BusinessRule("Project must have at least one OWNER")
            }
        }
        
        projectMemberRepository.updateRole(targetMember.id, role)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_MEMBER_ROLE_UPDATED,
            key = targetMember.id.toString(),
            event = ProjectMemberUpdatedRoleEvent(
                projectId = targetMember.projectId.toString(),
                userId = targetMember.userId.toString(),
                role = targetMember.role.name
            )
        )
        
        logger.info("User $userId updated role of $targetUserId to $role in project $projectId")
    }

    override suspend fun removeMember(projectId: UUID, userId: UUID, targetUserId: UUID) {
        val requestingMember = checkProjectAccess(projectId, userId, ProjectRole.MANAGER)
        
        if (requestingMember.role == ProjectRole.MANAGER && targetUserId == requestingMember.userId) {
            throw DomainException.Forbidden("Manager cannot remove themselves")
        }
        
        val targetMember = projectMemberRepository.findByProjectAndUser(projectId, targetUserId)
            ?: throw DomainException.NotFound("Target user is not a member of the project", targetUserId.toString())
            
        if (targetMember.role == ProjectRole.OWNER) {
            if (requestingMember.role != ProjectRole.OWNER) {
                throw DomainException.AccessDenied("Only OWNER can remove another OWNER")
            }
            
            val ownerCount = projectMemberRepository.findByProjectAndRole(projectId, ProjectRole.OWNER).size
            if (ownerCount <= 1) {
                throw DomainException.BusinessRule("Project must have at least one OWNER")
            }
        }
        
        projectMemberRepository.remove(targetMember.id)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_MEMBER_REMOVED,
            key = targetMember.id.toString(),
            event = ProjectMemberRemovedEvent(
                projectId = targetMember.projectId.toString(),
                userId = targetMember.userId.toString()
            )
        )
        
        logger.info("User $userId removed $targetUserId from project $projectId")
    }

    override suspend fun leaveProject(projectId: UUID, userId: UUID) {
        val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
            ?: throw DomainException.NotFound("User is not a member of the project", userId.toString())
            
        if (member.role == ProjectRole.OWNER) {
            val ownerCount = projectMemberRepository.findByProjectAndRole(projectId, ProjectRole.OWNER).size
            if (ownerCount <= 1) {
                throw DomainException.BusinessRule("Cannot leave project: you are the last OWNER. Please assign another OWNER first.")
            }
        }
        
        projectMemberRepository.remove(member.id)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_MEMBER_REMOVED,
            key = member.id.toString(),
            event = ProjectMemberRemovedEvent(
                projectId = member.projectId.toString(),
                userId = member.userId.toString()
            )
        )
        
        logger.info("User $userId left project $projectId")
    }
}