package com.quadro.project.domain.services

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectCreate
import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.ProjectMemberResponse
import com.quadro.project.domain.models.MemberRole
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

    private suspend fun getUserOrThrow(userId: UUID): User =
        userRepository.findById(userId) ?: throw DomainException.NotFound("User", userId.toString())

    private suspend fun checkProjectAccess(
        projectId: UUID,
        user: User,
        requiredRole: MemberRole
    ): ProjectMember {
        val member = projectMemberRepository.findByProjectAndUser(projectId, user.id)
            ?: throw DomainException.AccessDenied("User is not a member of the project")

        if (user.role == UserRole.SUPER_ADMIN) {
            return member.copy(role = MemberRole.OWNER)
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
        val requester = getUserOrThrow(userId)
        if (requester.role !in listOf(UserRole.ADMIN, UserRole.SUPER_ADMIN)) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        request.validate()
        if (projectRepository.existsByKey(request.key)) {
            logger.warn("User $userId attempted to create project with existing key: ${request.key}")
            throw DomainException.AlreadyExists("Project with key ${request.key}")
        }

        val now = Clock.System.now()
        val project = Project(
            id = UUID.randomUUID(),
            name = request.name,
            key = request.key,
            description = request.description,
            status = ProjectStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )

        val createdProject = projectRepository.upsert(project)

        val member = ProjectMember(
            id = UUID.randomUUID(),
            projectId = createdProject.id,
            userId = userId,
            role = MemberRole.OWNER,
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
        val user = getUserOrThrow(userId)
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", projectId.toString())

        checkProjectAccess(projectId, user, MemberRole.MANAGER)

        if (request.name != null && request.name != project.name && projectRepository.existsByName(request.name)) {
            logger.warn("User $userId attempted to rename project $projectId to existing name: ${request.name}")
            throw DomainException.AlreadyExists("Project with name ${request.name}")
        }

        val now = Clock.System.now()
        val updatedProject = project.copy(
            name = request.name ?: project.name,
            description = request.description ?: project.description,
            status = request.status ?: project.status,
            updatedAt = now
        )

        val saved = projectRepository.upsert(updatedProject)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_UPDATED,
            key = saved.id.toString(),
            event = ProjectUpdatedEvent(
                projectId = saved.id.toString(),
                updateBy = userId.toString(),
                name = saved.name,
                status = saved.status.name,
                key = saved.key
            )
        )

        logger.info("Updated Project ${saved.name} (ID: ${saved.id}) by user: $userId")
        return saved
    }

    override suspend fun deleteProject(userId: UUID, projectId: UUID) {
        val user = getUserOrThrow(userId)
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")

        if (user.role !in listOf(UserRole.SUPER_ADMIN, UserRole.ADMIN)) {
            checkProjectAccess(projectId, user, MemberRole.OWNER)
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
        return projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", projectId.toString())
    }

    override suspend fun findByName(name: String): Project {
        return projectRepository.findByName(name)
            ?: throw DomainException.NotFound("Project", name)
    }

    override suspend fun findByKey(key: String): Project {
        return projectRepository.findByKey(key)
            ?: throw DomainException.NotFound("Project", key)
    }

    override suspend fun findByUser(
        userId: UUID,
        limit: Int,
        offset: Int
    ): List<Project> {
        require(limit > 0) { "Limit must be positive" }
        require(offset >= 0) { "Offset must be non‑negative" }

        getUserOrThrow(userId)

        val projects = projectRepository.findByUser(userId, limit, offset)
        logger.info("User $userId requested list of projects, limit: $limit, offset: $offset, count: ${projects.size}")
        return projects
    }

    override suspend fun updateStatus(
        userId: UUID,
        projectId: UUID,
        status: ProjectStatus
    ): Boolean {
        val user = getUserOrThrow(userId)
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", "Project Not Found")

        checkProjectAccess(projectId, user, MemberRole.MANAGER)

        val result = projectRepository.updateStatus(projectId, status)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_UPDATED,
            key = project.id.toString(),
            event = ProjectUpdatedEvent(
                projectId = project.id.toString(),
                updateBy = userId.toString(),
                name = project.name,
                status = project.status.name,
                key = project.key
            )
        )

        logger.info("Project ${project.name} (ID: ${project.id}) status updated to $status by user: $userId")

        return result
    }

    override suspend fun getProjectMembers(
        projectId: UUID,
        userId: UUID,
        limit: Int,
        offset: Int
    ): List<ProjectMemberResponse> {
        require(limit > 0) { "Limit must be positive" }
        require(offset >= 0) { "Offset must be non‑negative" }
        val user = getUserOrThrow(userId)

        projectMemberRepository.findByProjectAndUser(projectId, user.id)
            ?: throw DomainException.NotFound("Project", projectId.toString())

        val members = projectMemberRepository.findByProject(projectId, limit, offset)

        if (members.isEmpty()) return emptyList()

        val userIds = members.map { it.userId }.distinct()
        val users = userRepository.findByIds(userIds)
        val userMap = users.associateBy { it.id }

        logger.info("User $userId requested members of project ${projectId}, limit: $limit")
        return members.mapNotNull { member ->
            val user = userMap[member.userId]
            if (user == null) {
                logger.warn("User ${member.userId} not found for project member ${member.id}")
                null
            } else {
                ProjectMemberResponse.from(member, user)
            }
        }
    }

    override suspend fun updateMemberRole(
        projectId: UUID,
        userId: UUID,
        targetUserId: UUID,
        role: MemberRole
    ) {
        if (userId == targetUserId) {
            logger.warn("User $userId attempted to change their own role in project $projectId")
            throw DomainException.Forbidden("Not allowed")
        }

        val requester = getUserOrThrow(userId)
        val requestingMember = checkProjectAccess(projectId, requester, MemberRole.MANAGER)

        if (!requestingMember.role.isAtLeast(role)) {
            throw DomainException.AccessDenied("Manager cannot grant OWNER roles")
        }

        val targetMember = projectMemberRepository.findByProjectAndUser(projectId, targetUserId)
            ?: throw DomainException.NotFound("Target user is not a member of the project", targetUserId.toString())

        if (targetMember.role == MemberRole.OWNER && role != MemberRole.OWNER) {
            if (requestingMember.role != MemberRole.OWNER) {
                throw DomainException.AccessDenied("Only OWNER can modify another OWNER's role")
            }
            val ownerCount = projectMemberRepository.findByProjectAndRole(projectId, MemberRole.OWNER).size
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
        val requester = getUserOrThrow(userId)
        val requestingMember = checkProjectAccess(projectId, requester, MemberRole.MANAGER)

        if (requestingMember.role == MemberRole.MANAGER && targetUserId == requester.id) {
            throw DomainException.Forbidden("Manager cannot remove themselves")
        }

        val targetMember = projectMemberRepository.findByProjectAndUser(projectId, targetUserId)
            ?: throw DomainException.NotFound("Target user is not a member of the project", targetUserId.toString())

        if (targetMember.role == MemberRole.OWNER) {
            if (requestingMember.role != MemberRole.OWNER) {
                throw DomainException.AccessDenied("Only OWNER can remove another OWNER")
            }
            val ownerCount = projectMemberRepository.findByProjectAndRole(projectId, MemberRole.OWNER).size
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

        if (member.role == MemberRole.OWNER) {
            val ownerCount = projectMemberRepository.findByProjectAndRole(projectId, MemberRole.OWNER).size
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