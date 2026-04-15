package com.quadro.team.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamCreate
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamResponse
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamUpdate
import com.quadro.team.domain.repositories.CompanyMemberRepository
import com.quadro.team.domain.repositories.CompanyRepository
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamRepository
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class TeamServiceImpl(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val companyRepository: CompanyRepository,
    private val memberRepository: CompanyMemberRepository
) : TeamService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun create(companyId: UUID, createdBy: UUID, request: TeamCreate): TeamResponse {
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", "Company with id $companyId not found")

        val member = memberRepository.findByCompanyAndUser(companyId, createdBy)
            ?: throw DomainException.NotFound("Member", "Member with id $companyId not found")
        request.validate()

        if (!company.createRole.isAtLeast(member.role)) {
            throw DomainException.AccessDenied("Create role ${member.role} is not allowed")
        }

        request.leadId?.let {
            memberRepository.findByCompanyAndUser(companyId, UUID.fromString(request.leadId))
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

    override suspend fun update(id: UUID, request: TeamUpdate): TeamResponse {
        val team = teamRepository.findById(id) ?: throw DomainException.NotFound("Team", id.toString())
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
        if (team.createdBy != requesterId) throw DomainException.Forbidden("Only creator can delete team")
        teamRepository.delete(id)
        logger.info("Deleted team id=$id by user $requesterId")
    }
}