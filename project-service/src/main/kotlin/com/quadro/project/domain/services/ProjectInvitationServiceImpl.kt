package com.quadro.project.domain.services

import com.quadro.project.domain.models.InvitationCreate
import com.quadro.project.domain.models.InvitationResponse
import com.quadro.project.domain.models.InvitationStatus
import com.quadro.project.domain.models.ProjectInvitation
import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.ProjectRole
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
    private val eventProducer: EventProducer,
    private val config: DomainConfig
) : ProjectInvitationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createInvitation(
        projectId: UUID,
        userId: UUID,
        request: InvitationCreate
    ): InvitationResponse {
        val inviter = projectMemberRepository.findByProjectAndUser(projectId, userId)
        if (inviter == null || inviter.role !in listOf(ProjectRole.OWNER, ProjectRole.MANAGER)) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val pendingCount = projectInvitationRepository.countPendingByProject(projectId)
        if (pendingCount >= 50) {
            throw DomainException.BusinessRule("Too many pending invitations")
        }

        val now = Clock.System.now()
        val expiresAt = now.plus(
            request.expiresInDays?.days ?: 8.days
        )

        val invitation = ProjectInvitation(
            id = UUID.randomUUID(),
            projectId = projectId,
            invitedBy = userId,
            inviteType = request.inviteType,
            identifier = request.identifier ?: "link:${UUID.randomUUID()}",
            role = request.role,
            status = InvitationStatus.PENDING,
            token = "",
            expiresAt = expiresAt,
            createdAt = now,
            acceptedAt = null,
            acceptedBy = null,
            message = request.message
        )

        val token = invitationTokenService.generateToken(
            invitationId = invitation.id,
            projectId = invitation.projectId,
            expiresInDays = request.expiresInDays
        )

        val finalInvitation = invitation.copy(token = token)
        projectInvitationRepository.create(finalInvitation)

        val inviteLink = "${config.domain}/invite?token=$token"
        val result = InvitationResponse.from(finalInvitation, inviteLink)

        logger.info("Invitation created: ${invitation.id} for project: $projectId by user: $userId")
        return result
    }

    override suspend fun acceptInvitation(
        token: String,
        userId: UUID
    ): ProjectResponse {
        val validation = invitationTokenService.validateToken(token)
        if (!validation.isValid) {
            throw DomainException.ValidationError(validation.error ?: "Invalid invitation")
        }

        val invitation = projectInvitationRepository.findById(validation.invitationId!!)
            ?: throw DomainException.NotFound("Invitation", validation.invitationId.toString())

        if (invitation.status != InvitationStatus.PENDING) {
            throw DomainException.BusinessRule("Invitation is no longer valid")
        }

        if (invitation.expiresAt < Clock.System.now()) {
            projectInvitationRepository.updateStatus(invitation.id, InvitationStatus.EXPIRED)
            throw DomainException.BusinessRule("Invitation has expired")
        }

        val project = projectRepository.findById(invitation.projectId)
            ?: throw DomainException.NotFound("Project", invitation.projectId.toString())

        if (projectMemberRepository.exists(invitation.projectId, userId)) {
            logger.info("User $userId is already a member of project ${project.id}, invitation remains PENDING")
            return ProjectResponse.from(project)
        }

        val member = ProjectMember(
            id = UUID.randomUUID(),
            projectId = invitation.projectId,
            userId = userId,
            role = invitation.role,
            joinedAt = Clock.System.now(),
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

        projectInvitationRepository.acceptInvitation(invitation.id, userId)

        logger.info("Invitation accepted: ${invitation.id} by user: $userId")
        return ProjectResponse.from(project)
    }

    override suspend fun getInvitations(
        projectId: UUID,
        userId: UUID
    ): List<InvitationResponse> {
        val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
        if (member == null || (member.role != ProjectRole.OWNER)) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val project = projectRepository.findById(projectId)
            ?: throw DomainException.NotFound("Project", projectId.toString())

        val invitations = projectInvitationRepository.findByProject(projectId, null)
        return invitations.map { invitation ->
            val inviteLink = "${config.domain}/invite?token=${invitation.token}"
            InvitationResponse.from(invitation, inviteLink)
        }
    }

    override suspend fun cancelInvitation(
        projectId: UUID,
        userId: UUID,
        invitationId: UUID
    ) {
        val member = projectMemberRepository.findByProjectAndUser(projectId, userId)
        if (member == null || (member.role != ProjectRole.OWNER)) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val invitation = projectInvitationRepository.findById(invitationId)
            ?: throw DomainException.NotFound("Invitation", invitationId.toString())

        if (invitation.projectId != projectId) {
            throw DomainException.BusinessRule("Invitation does not belong to this project")
        }

        if (invitation.status != InvitationStatus.PENDING) {
            throw DomainException.BusinessRule("Only pending invitations can be cancelled")
        }

        projectInvitationRepository.updateStatus(invitationId, InvitationStatus.CANCELLED)

        logger.info("Invitation cancelled: $invitationId by user: $userId")
    }
}