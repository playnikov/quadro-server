package com.quadro.team.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Company
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamCreate
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamResponse
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamUpdate
import com.quadro.team.domain.models.User
import com.quadro.team.domain.repositories.CompanyMemberRepository
import com.quadro.team.domain.repositories.CompanyRepository
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamRepository
import com.quadro.team.domain.repositories.UserRepository
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class TeamServiceImpl(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val companyRepository: CompanyRepository,
    private val userRepository: UserRepository,
    private val companyMemberRepository: CompanyMemberRepository
) : TeamService {
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

        if (!member.role.isAtLeast(company.teamManagementRole)) {
            logger.warn("User $userId (role: ${member.role}) denied team management access for $action")
            throw DomainException.AccessDenied("Insufficient permissions to $action teams")
        }

        return user to company
    }

    override suspend fun create(companyId: UUID, createdBy: UUID, request: TeamCreate): TeamResponse {
        ensureUserCanManageProjects(createdBy, companyId, "create")

        request.leadId?.let {
            companyMemberRepository.findByCompanyAndUser(companyId, UUID.fromString(request.leadId))
                ?: throw DomainException.NotFound("Member", "Member ${request.leadId} with id $companyId not found")
        }

        if (teamRepository.existsByNameInCompany(companyId, request.name)) {
            logger.warn("Attempt to create duplicate team '${request.name}' in company $companyId by user $createdBy")
            throw DomainException.AlreadyExists("Team '${request.name}' in this company")
        }

        val now = Clock.System.now()
        val team = teamRepository.create(
            Team(
                id = UUID.randomUUID(),
                companyId = companyId,
                name = request.name,
                description = request.description,
                avatar = request.avatar,
                status = TeamStatus.ACTIVE,
                visibility = request.visibility,
                leadId = UUID.fromString(request.leadId),
                createdBy = createdBy,
                createdAt = now,
                updatedAt = now
            )
        )

        teamMemberRepository.add(
            TeamMember(
                id = UUID.randomUUID(),
                teamId = team.id,
                userId = UUID.fromString(request.leadId),
                role = TeamRole.LEAD,
                joinedAt = now,
                invitedBy = createdBy,
                isActive = true,
                lastActiveAt = null
            )
        )

        logger.info("Created Team id=${team.id}, name='${request.name}', company=$companyId, createdBy=$createdBy")
        return TeamResponse.from(team)
    }

    override suspend fun getById(id: UUID): TeamResponse {
        val team = teamRepository.findById(id) ?: throw DomainException.NotFound("Team", id.toString())
        logger.info("Retrieved team: id=$id, name=${team.name}")
        return TeamResponse.from(team)
    }

    override suspend fun getByCompany(
        companyId: UUID,
        page: Int,
        size: Int
    ): List<TeamResponse> =
        teamRepository.findByCompany(companyId, page, size)
            .map { TeamResponse.from(it) }

    override suspend fun update(id: UUID, request: TeamUpdate, requesterId: UUID): TeamResponse {
        val team = teamRepository.findById(id) ?: throw DomainException.NotFound("Team", id.toString())
        ensureUserCanManageProjects(requesterId, team.companyId, "update")
        val teamUpdate = teamRepository.update(
            team.copy(
                name = request.name?.trim() ?: team.name,
                description = request.description?.trim() ?: team.description,
                leadId = request.leadId?.let { UUID.fromString(request.leadId) } ?: team.leadId,
                avatar = request.avatar?.trim() ?: team.avatar,
                visibility = request.visibility ?: team.visibility,
                status = request.status ?: team.status
            )
        )
        logger.info("Updated team id=$id, changes: name=${request.name ?: "unchanged"}, leadId=${request.leadId ?: "unchanged"}")
        return TeamResponse.from(teamUpdate)
    }

    override suspend fun delete(id: UUID, requesterId: UUID) {
        val team = teamRepository.findById(id) ?: throw DomainException.NotFound("Team", id.toString())
        ensureUserCanManageProjects(requesterId, team.companyId, "delete")
        if (team.createdBy != requesterId) throw DomainException.Forbidden("Only creator can delete team")
        teamRepository.delete(id)
        logger.info("Deleted team id=$id by user $requesterId")
    }
}