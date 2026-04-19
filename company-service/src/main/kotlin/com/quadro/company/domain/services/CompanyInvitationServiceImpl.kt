package com.quadro.company.domain.services

import com.quadro.company.domain.models.CompanyInvitation
import com.quadro.company.domain.models.CompanyMember
import com.quadro.company.domain.models.CompanyResponse
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.models.InvitationCreate
import com.quadro.company.domain.models.InvitationResponse
import com.quadro.company.domain.models.InvitationStatus
import com.quadro.company.domain.repositories.CompanyInvitationRepository
import com.quadro.company.domain.repositories.CompanyMemberRepository
import com.quadro.company.domain.repositories.CompanyRepository
import com.quadro.company.domain.repositories.UserRepository
import com.quadro.shared.data.config.DomainConfig
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.CompanyMemberAddedEvent
import com.quadro.shared.dto.DomainException
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class CompanyInvitationServiceImpl(
    private val companyInvitationRepository: CompanyInvitationRepository,
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val invitationTokenService: InvitationTokenService,
    private val userRepository: UserRepository,
    private val eventProducer: EventProducer,
    private val config: DomainConfig
) : CompanyInvitationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createInvitation(
        companyId: UUID,
        userId: UUID,
        request: InvitationCreate
    ): InvitationResponse {
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())

        val inviter = companyMemberRepository.findByCompanyAndUser(companyId, userId)
        if (inviter == null || inviter.role !in listOf(CompanyRole.OWNER, CompanyRole.ADMIN, CompanyRole.MANAGER)) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val pendingCount = companyInvitationRepository.countPendingByCompany(companyId)
        if (pendingCount >= 50) {
            throw DomainException.BusinessRule("Too many pending invitations")
        }

        val now = Clock.System.now()
        val expiresAt = now.plus(
            request.expiresInDays?.days ?: company.companySettings.inviteExpiryDays.days
        )

        val invitation = CompanyInvitation(
            id = UUID.randomUUID(),
            companyId = companyId,
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
            companyId = invitation.companyId,
            expiresInDays = request.expiresInDays ?: company.companySettings.inviteExpiryDays
        )

        val finalInvitation = invitation.copy(token = token)
        companyInvitationRepository.create(finalInvitation)

        val inviteLink = "${config.domain}/invite?token=$token"
        val result = InvitationResponse.fromCompanyInvitation(company, finalInvitation, inviteLink)

        logger.info("Invitation created: ${invitation.id} for company: $companyId by user: $userId")
        return result
    }

    override suspend fun acceptInvitation(
        token: String,
        userId: UUID
    ): CompanyResponse {
        val validation = invitationTokenService.validateToken(token)
        if (!validation.isValid) {
            throw DomainException.ValidationError(validation.error ?: "Invalid invitation")
        }

        val invitation = companyInvitationRepository.findById(validation.invitationId!!)
            ?: throw DomainException.NotFound("Invitation", validation.invitationId.toString())

        if (invitation.status != InvitationStatus.PENDING) {
            throw DomainException.BusinessRule("Invitation is no longer valid")
        }

        if (invitation.expiresAt < Clock.System.now()) {
            companyInvitationRepository.updateStatus(invitation.id, InvitationStatus.EXPIRED)
            throw DomainException.BusinessRule("Invitation has expired")
        }

        val company = companyRepository.findById(invitation.companyId)
            ?: throw DomainException.NotFound("Company", invitation.companyId.toString())

        if (companyMemberRepository.exists(invitation.companyId, userId)) {
            logger.info("User $userId is already a member of company ${company.id}, invitation remains PENDING")
            return CompanyResponse.from(company)
        }

        val member = CompanyMember(
            id = UUID.randomUUID(),
            companyId = invitation.companyId,
            userId = userId,
            role = invitation.role,
            joinedAt = Clock.System.now(),
            invitedBy = invitation.invitedBy,
            invitedAt = invitation.createdAt,
            lastActiveAt = Clock.System.now()
        )

        companyMemberRepository.add(member)

        eventProducer.publish(
            topic = KafkaTopics.COMPANY_MEMBER_ADDED,
            key = member.id.toString(),
            event = CompanyMemberAddedEvent(
                memberId = member.id.toString(),
                companyId = member.companyId.toString(),
                userId = member.userId.toString(),
                role = member.role.name,
                invitedBy = member.invitedBy.toString()
            )
        )

        companyInvitationRepository.acceptInvitation(invitation.id, userId)

        logger.info("Invitation accepted: ${invitation.id} by user: $userId")
        return CompanyResponse.from(company)
    }

    override suspend fun getInvitations(
        companyId: UUID,
        userId: UUID
    ): List<InvitationResponse> {
        val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
        if (member == null || (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN)) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())

        val invitations = companyInvitationRepository.findByCompany(companyId, null)
        return invitations.map { invitation ->
            val inviteLink = "${config.domain}/invite?token=${invitation.token}"
            InvitationResponse.fromCompanyInvitation(company, invitation, inviteLink)
        }
    }

    override suspend fun cancelInvitation(
        companyId: UUID,
        userId: UUID,
        invitationId: UUID
    ) {
        val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
        if (member == null || (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN)) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val invitation = companyInvitationRepository.findById(invitationId)
            ?: throw DomainException.NotFound("Invitation", invitationId.toString())

        if (invitation.companyId != companyId) {
            throw DomainException.BusinessRule("Invitation does not belong to this company")
        }

        if (invitation.status != InvitationStatus.PENDING) {
            throw DomainException.BusinessRule("Only pending invitations can be cancelled")
        }

        companyInvitationRepository.updateStatus(invitationId, InvitationStatus.CANCELLED)

        logger.info("Invitation cancelled: $invitationId by user: $userId")
    }
}