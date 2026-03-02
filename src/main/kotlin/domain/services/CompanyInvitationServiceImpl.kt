package com.quadro.domain.services

import com.quadro.datasource.repositories.CompanyInvitationRepository
import com.quadro.datasource.repositories.CompanyMemberRepository
import com.quadro.datasource.repositories.CompanyRepository
import com.quadro.datasource.repositories.UserRepository
import com.quadro.domain.models.CompanyInvitation
import com.quadro.domain.models.CompanyMember
import com.quadro.domain.models.CompanyResult
import com.quadro.domain.models.CompanyRole
import com.quadro.domain.models.InvitationCreate
import com.quadro.domain.models.InvitationResult
import com.quadro.domain.models.InvitationStatus
import com.quadro.security.JwtInvitationTokenService
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import java.util.UUID

class CompanyInvitationServiceImpl(
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val companyInvitationRepository: CompanyInvitationRepository,
    private val userRepository: UserRepository,
    private val invitationTokenService: JwtInvitationTokenService
) : CompanyInvitationService {
    private val config = ConfigFactory.load().getConfig("ktor")
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createInvitation(
        companyId: UUID,
        userId: UUID,
        request: InvitationCreate
    ): Result<InvitationResult> {
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

            val now = System.currentTimeMillis()
            val expiresIn = request.expiresInDays?.times(24 * 60 * 60 * 1000L)
                ?: (company.companySettings.invitationExpiryDays * 24 * 60 * 60 * 1000L)


            val invitation = CompanyInvitation(
                id = UUID.randomUUID(),
                companyId = companyId,
                teamId = request.teamId,
                invitedBy = userId,
                role = request.role,
                status = InvitationStatus.PENDING,
                token = "",
                expiresAt = now + expiresIn,
                createdAt = now,
                acceptedAt = null,
                message = request.message,
                acceptedBy = null
            )

            val token = invitationTokenService.generateToken(
                invitationId = invitation.id,
                companyId = invitation.companyId,
                teamId = invitation.teamId,
                expiresInDays = request.expiresInDays ?: company.companySettings.invitationExpiryDays
            )

            val finalInvitation = invitation.copy(token = token)
            companyInvitationRepository.create(finalInvitation)

            val inviteLink = "${config.getString("domain.host")}/invite?token=$token"

            val inviterUser = userRepository.findById(userId)!!

            val result = InvitationResult(
                id = invitation.id,
                companyId = invitation.companyId,
                companyName = company.name,
                teamId = invitation.teamId,
                teamName = null,
                invitedBy = userId,
                invitedByEmail = inviterUser.email,
                invitedByName = inviterUser.lastName + " " + inviterUser.firstName,
                role = request.role,
                status = InvitationStatus.PENDING,
                token = token,
                expiresAt = invitation.expiresAt,
                createdAt = invitation.createdAt,
                message = invitation.message,
                inviteLink = inviteLink
            )

            logger.info("Invitation created: ${invitation.id} for company: $companyId by user: $userId")
            Result.success(result)
        } catch (e: Exception) {
            logger.error("Failed to create invitation", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptInvitation(
        userId: UUID,
        token: String
    ): Result<CompanyResult> {
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

            if (invitation.expiresAt < System.currentTimeMillis()) {
                companyInvitationRepository.updateStatus(invitation.id, InvitationStatus.EXPIRED)
                return Result.failure(Exception("Invitation has expired"))
            }

            val company = companyRepository.findById(invitation.companyId)!!

            if (companyMemberRepository.exists(invitation.companyId, userId)) {
                logger.info("User $userId is already a member of company ${company.id}, invitation remains PENDING")
                return Result.success(CompanyResult.fromCompany(company))
            }

            val member = CompanyMember(
                id = UUID.randomUUID(),
                companyId = invitation.companyId,
                userId = userId,
                role = invitation.role,
                joinedAt = System.currentTimeMillis(),
                invitedBy = invitation.invitedBy,
                invitedAt = invitation.createdAt,
                isActive = true
            )

            companyMemberRepository.add(member)

            invitation.teamId?.let { teamId ->
                // Здесь добавить в команду
                logger.info("User $userId will be added to team: $teamId")
            }

            companyInvitationRepository.acceptInvitation(invitation.id, userId)

            logger.info("Invitation accepted: ${invitation.id} by user: $userId")
            Result.success(CompanyResult.fromCompany(company))
        } catch (e: Exception) {
            logger.error("Failed to accept invitation", e)
            Result.failure(e)
        }
    }

    override suspend fun getInvitations(
        companyId: UUID,
        userId: UUID
    ): Result<List<InvitationResult>> {
        return try {
            val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            if (member == null || (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val company = companyRepository.findById(companyId)
                ?: return Result.failure(Exception("Company not found"))

            val invitations = companyInvitationRepository.findByCompany(companyId, null)

            val results = invitations.map { invitation ->
                val inviterUser = userRepository.findById(invitation.invitedBy)!!

                InvitationResult(
                    id = invitation.id,
                    companyId = invitation.companyId,
                    companyName = company.name,
                    teamId = invitation.teamId,
                    teamName = null,
                    invitedBy = invitation.invitedBy,
                    invitedByEmail = inviterUser.email,
                    invitedByName = "${inviterUser.lastName} ${inviterUser.firstName}".trim(),
                    role = invitation.role,
                    status = invitation.status,
                    token = invitation.token,
                    expiresAt = invitation.expiresAt,
                    createdAt = invitation.createdAt,
                    message = invitation.message,
                    inviteLink = "https://quadro.com/invite?token=${invitation.token}"
                )
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

    override suspend fun resendInvitation(
        companyId: UUID,
        userId: UUID,
        invitationId: UUID
    ): Result<InvitationResult> {
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
                return Result.failure(Exception("Only pending invitations can be resent"))
            }

            val company = companyRepository.findById(companyId)!!

            val now = System.currentTimeMillis()
            val newExpiresAt = now + (company.companySettings.invitationExpiryDays * 24 * 60 * 60 * 1000L)

            val newToken = invitationTokenService.generateToken(
                invitationId = invitation.id,
                companyId = invitation.companyId,
                teamId = invitation.teamId,
                expiresInDays = company.companySettings.invitationExpiryDays
            )

            val updatedInvitation = invitation.copy(
                token = newToken,
                expiresAt = newExpiresAt,
                createdAt = now
            )

            val inviterUser = userRepository.findById(userId)!!

            val result = InvitationResult(
                id = updatedInvitation.id,
                companyId = updatedInvitation.companyId,
                companyName = company.name,
                teamId = updatedInvitation.teamId,
                teamName = null,
                invitedBy = updatedInvitation.invitedBy,
                invitedByEmail = inviterUser.email,
                invitedByName = "${inviterUser.lastName} ${inviterUser.firstName}".trim(),
                role = updatedInvitation.role,
                status = updatedInvitation.status,
                token = newToken,
                expiresAt = newExpiresAt,
                createdAt = now,
                message = updatedInvitation.message,
                inviteLink = "https://quadro.com/invite?token=$newToken"
            )

            logger.info("Invitation resent: $invitationId by user: $userId")
            Result.success(result)
        } catch (e: Exception) {
            logger.error("Failed to resend invitation", e)
            Result.failure(e)
        }
    }
}