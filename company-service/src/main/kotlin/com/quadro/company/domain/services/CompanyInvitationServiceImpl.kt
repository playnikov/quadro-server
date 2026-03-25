package com.quadro.company.domain.services

import com.quadro.company.config.AppConfig
import com.quadro.company.domain.models.Company
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
    private val appConfig: AppConfig
) : CompanyInvitationService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createInvitation(
        companyId: UUID,
        userId: UUID,
        request: InvitationCreate
    ): Result<InvitationResponse> {
        return try {
            val company = companyRepository.findById(companyId)
                ?: return Result.failure(Exception("Company not found"))

            val inviter = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            if (inviter == null || inviter.role !in listOf(CompanyRole.OWNER, CompanyRole.ADMIN, CompanyRole.MANAGER)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val pendingCount = companyInvitationRepository.countPendingByCompany(companyId)
            if (pendingCount >= 50) {
                return Result.failure(Exception("Too many pending invitations"))
            }

            val now = Clock.System.now()
            val expiresAt = Clock.System.now().plus(
                request.expiresInDays?.days ?: company.companySettings.invitationExpiryDays.days
            )

            val invitation = CompanyInvitation(
                id = UUID.randomUUID(),
                companyId = companyId,
                teamId = request.teamId,
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
                teamId = invitation.teamId,
                expiresInDays = request.expiresInDays ?: company.companySettings.invitationExpiryDays
            )

            val finalInvitation = invitation.copy(token = token)
            companyInvitationRepository.create(finalInvitation)

            val inviteLink = "${appConfig.server.domain}/invite?token=$token"

            val result = InvitationResponse.fromCompanyInvitation(company, finalInvitation, inviteLink)

            logger.info("Invitation created: ${invitation.id} for company: $companyId by user: $userId")
            Result.success(result)
        } catch (e: Exception) {
            logger.error("Failed to create invitation", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptInvitation(
        token: String,
        userId: UUID
    ): Result<CompanyResponse> {
        return try {
            val validation = invitationTokenService.validateToken(token)
            if (!validation.isValid) {
                return Result.failure(Exception(validation.error ?: "Invalid invitation"))
            }

            val invitation = companyInvitationRepository.findById(validation.invitationId!!)
                ?: return Result.failure(Exception("Invitation not found"))

            if (invitation.status != InvitationStatus.PENDING) {
                return Result.failure(Exception("Invitation is no longer valid"))
            }

            if (invitation.expiresAt < Clock.System.now()) {
                companyInvitationRepository.updateStatus(invitation.id, InvitationStatus.EXPIRED)
                return Result.failure(Exception("Invitation has expired"))
            }

            val company = companyRepository.findById(invitation.companyId)!!

            if (companyMemberRepository.exists(invitation.companyId, userId)) {
                logger.info("User $userId is already a member of company ${company.id}, invitation remains PENDING")
                return Result.success(CompanyResponse.fromCompany(company))
            }

            val member = CompanyMember(
                id = UUID.randomUUID(),
                companyId = invitation.companyId,
                userId = userId,
                role = invitation.role,
                joinedAt = Clock.System.now(),
                invitedBy = invitation.invitedBy,
                invitedAt = invitation.createdAt,
                lastActiveAt = Clock.System.now(),
                isActive = true
            )

            companyMemberRepository.add(member)

            invitation.teamId?.let { teamId ->
                // Здесь добавить в команду
                logger.info("User $userId will be added to team: $teamId")
            }

            companyInvitationRepository.acceptInvitation(invitation.id, userId)

            logger.info("Invitation accepted: ${invitation.id} by user: $userId")
            Result.success(CompanyResponse.fromCompany(company))
        } catch (e: Exception) {
            logger.error("Failed to accept invitation", e)
            Result.failure(e)
        }
    }

    override suspend fun getInvitations(
        companyId: UUID,
        userId: UUID
    ): Result<List<InvitationResponse>> {
        return try {
            val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            if (member == null || (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val company = companyRepository.findById(companyId)
                ?: return Result.failure(Exception("Company not found"))

            val invitations = companyInvitationRepository.findByCompany(companyId, null)

            val results = invitations.map { invitation ->
                val inviteLink = "${appConfig.server.domain}/invite?token=${invitation.token}"
                InvitationResponse.fromCompanyInvitation(company, invitation, inviteLink)
            }
            Result.success(results)
        } catch (e: Exception) {
            logger.error("Failed to get invitations", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelInvitation(
        companyId: UUID,
        userId: UUID,
        invitationId: UUID
    ): Result<Unit> {
        return try {
            val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            if (member == null || (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val invitation = companyInvitationRepository.findById(invitationId)
                ?: return Result.failure(Exception("Invitation not found"))

            if (invitation.companyId != companyId) {
                return Result.failure(Exception("Invitation does not belong to this company"))
            }

            if (invitation.status != InvitationStatus.PENDING) {
                return Result.failure(Exception("Only pending invitations can be cancelled"))
            }

            companyInvitationRepository.updateStatus(invitationId, InvitationStatus.CANCELLED)

            logger.info("Invitation cancelled: $invitationId by user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to cancel invitation", e)
            Result.failure(e)
        }
    }
}