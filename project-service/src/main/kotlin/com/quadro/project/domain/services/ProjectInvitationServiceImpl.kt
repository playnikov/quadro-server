package com.quadro.project.domain.services

import com.quadro.project.domain.models.InvitationCreate
import com.quadro.project.domain.models.InvitationResponse
import com.quadro.project.domain.models.InviteStatus
import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.ProjectInvitation
import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.MemberRole
import com.quadro.project.domain.models.User
import com.quadro.project.domain.repositories.ProjectInvitationRepository
import com.quadro.project.domain.repositories.ProjectMemberRepository
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.domain.repositories.UserRepository
import com.quadro.project.presentation.models.ProjectResponse
import com.quadro.shared.data.config.DomainConfig
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.ProjectMemberAddedEvent
import com.quadro.shared.dto.DomainException
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class ProjectInvitationServiceImpl(
    private val projectInvitationRepository: ProjectInvitationRepository,
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val invitationTokenService: InvitationTokenService,
    private val userRepository: UserRepository,
    private val eventProducer: EventProducer,
    private val config: DomainConfig
) : ProjectInvitationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun getUserOrThrow(userId: UUID): User =
        userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())

    private suspend fun checkProjectManagePermission(projectId: UUID, userId: UUID): ProjectMember {
        val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
        if (member == null || !member.role.isAtLeast(MemberRole.MANAGER)) {
            throw DomainException.AccessDenied("Insufficient permissions: need OWNER or MANAGER")
        }
        return member
    }

    private suspend fun checkProjectOwnerPermission(projectId: UUID, userId: UUID): ProjectMember {
        val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
        if (member == null || member.role != MemberRole.OWNER) {
            throw DomainException.AccessDenied("Insufficient permissions: need OWNER")
        }
        return member
    }

    override suspend fun createInvitation(
        projectId: UUID,
        userId: UUID,
        request: InvitationCreate
    ): InvitationResponse {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", projectId.toString())

        checkProjectManagePermission(projectId, userId)

        if (request.type == InviteType.EMAIL && request.identifier != null) {
            val existingUser = userRepository.findByEmail(request.identifier)
            if (existingUser != null && projectMemberRepository.exists(projectId, existingUser.id)) {
                throw DomainException.BusinessRule("User is already a member of the project")
            }
        }

        val now = Clock.System.now()
        val expiresAt = now.plus(
            request.expiresInDays?.days ?: 7.days
        )

        val identifier = when (request.type) {
            InviteType.EMAIL -> request.identifier
                ?: throw DomainException.ValidationError("Email is required for EMAIL invitation")
            InviteType.LINK -> "link"
        }

        val invitation = ProjectInvitation(
            id = UUID.randomUUID(),
            projectId = projectId,
            invitedBy = userId,
            type = request.type,
            identifier = identifier,
            role = request.role,
            status = InviteStatus.PENDING,
            token = "", // временно
            expiresAt = expiresAt,
            createdAt = now,
            acceptedAt = null,
            acceptedBy = null,
            message = request.message
        )

        val token = invitationTokenService.generateToken(
            invitationId = invitation.id,
            projectId = invitation.projectId
        )

        val finalInvitation = invitation.copy(token = token)
        projectInvitationRepository.create(finalInvitation)

        val inviteLink = "${config.domain}/api/projects/invite?token=$token"
        val result = InvitationResponse.from(project.name, finalInvitation, inviteLink)

        logger.info("Invitation created: ${invitation.id} for project: $projectId by user: $userId")
        return result
    }

    override suspend fun acceptInvitation(
        token: String,
        userId: UUID
    ): ProjectResponse {
        val user = getUserOrThrow(userId)

        val validation = invitationTokenService.validateToken(token)
        if (!validation.isValid) {
            throw DomainException.ValidationError(validation.error ?: "Invalid invitation token")
        }

        val invitation = projectInvitationRepository.findById(validation.invitationId!!)
            ?: throw DomainException.NotFound("Invitation", validation.invitationId.toString())

        if (invitation.status != InviteStatus.PENDING) {
            throw DomainException.BusinessRule("Invitation is no longer valid (status: ${invitation.status})")
        }

        val now = Clock.System.now()
        if (invitation.expiresAt < now) {
            projectInvitationRepository.updateStatus(invitation.id, InviteStatus.EXPIRED)
            throw DomainException.BusinessRule("Invitation has expired")
        }

        val project = projectRepository.findById(invitation.projectId)
            ?: throw DomainException.NotFound("Project", invitation.projectId.toString())
        if (projectMemberRepository.exists(invitation.projectId, user.id)) {
            logger.info("User ${user.id} is already a member of project ${project.id}")
            return ProjectResponse.from(project)
        }

        val member = ProjectMember(
            id = UUID.randomUUID(),
            projectId = invitation.projectId,
            userId = user.id,
            role = invitation.role,
            joinedAt = now,
            invitedBy = invitation.invitedBy,
            invitedAt = invitation.createdAt
        )

        projectMemberRepository.add(member)

        eventProducer.publish(
            topic = KafkaTopics.PROJECT_MEMBER_ADDED,
            key = member.id.toString(),
            event = ProjectMemberAddedEvent(
                projectId = member.projectId.toString(),
                userId = member.userId.toString(),
                role = member.role.name
            )
        )

        if (invitation.type == InviteType.EMAIL) {
            projectInvitationRepository.acceptInvitation(invitation.id, user.id)
        }

        logger.info("Invitation accepted: ${invitation.id} by user ${user.id}")
        return ProjectResponse.from(project)
    }

    override suspend fun getInvitations(
        projectId: UUID,
        userId: UUID
    ): List<InvitationResponse> {
        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", projectId.toString())

        checkProjectOwnerPermission(projectId, userId)

        val invitations = projectInvitationRepository.findByProject(projectId)

        return invitations.map { invitation ->
            val inviteLink = "${config.domain}/invite?token=${invitation.token}"
            InvitationResponse.from(project.name, invitation, inviteLink)
        }
    }

    override suspend fun getInvitations(email: String): List<InvitationResponse> {
        val invitations = projectInvitationRepository.findByEmail(email)
            .filter { it.status == InviteStatus.PENDING && it.expiresAt > Clock.System.now() }
        return invitations.map { invitation ->
            val project = projectRepository.findById(invitation.projectId)
            InvitationResponse.from(project?.name ?: "Неизвестное название", invitation, "")
        }
    }

    override suspend fun cancelInvitation(
        projectId: UUID,
        userId: UUID,
        invitationId: UUID
    ) {
        checkProjectOwnerPermission(projectId, userId)

        val invitation = projectInvitationRepository.findById(invitationId)
            ?: throw DomainException.NotFound("Invitation", invitationId.toString())

        if (invitation.projectId != projectId) {
            throw DomainException.BusinessRule("Invitation does not belong to this project")
        }

        if (invitation.status != InviteStatus.PENDING) {
            throw DomainException.BusinessRule("Only pending invitations can be cancelled")
        }

        projectInvitationRepository.updateStatus(invitationId, InviteStatus.CANCELLED)

        logger.info("Invitation cancelled: $invitationId by user: $userId")
    }
}