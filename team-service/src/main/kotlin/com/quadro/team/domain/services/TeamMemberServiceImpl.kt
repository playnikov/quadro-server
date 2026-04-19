package com.quadro.team.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamMemberResponse
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.repositories.CompanyMemberRepository
import com.quadro.team.domain.repositories.CompanyRepository
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamRepository
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class TeamMemberServiceImpl(
    private val companyRepository: CompanyRepository,
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val companyMemberRepository: CompanyMemberRepository
) : TeamMemberService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun canManageTeam(teamId: UUID, requesterId: UUID) {
        val team = teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        val company = companyRepository.findById(team.companyId)
            ?: throw DomainException.NotFound("Company", "ID: ${team.companyId}")
        val member = companyMemberRepository.findByCompanyAndUser(team.companyId, requesterId)
            ?: throw DomainException.NotFound("Member", "Member with id ${team.companyId} not found")

        if (!member.role.isAtLeast(company.teamManagementRole)) {
            logger.warn("User $requesterId (role: ${member.role}) denied team management access")
            throw DomainException.AccessDenied("Insufficient permissions")
        }
    }

    override suspend fun getMembers(teamId: UUID): List<TeamMemberResponse> {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        return teamMemberRepository.findByTeam(teamId).map { TeamMemberResponse.from(it) }
    }

    override suspend fun addMember(
        teamId: UUID,
        userId: UUID,
        role: TeamRole,
        requesterId: UUID
    ): TeamMemberResponse {
        canManageTeam(teamId, requesterId)

        if (teamMemberRepository.exists(teamId, userId)) throw DomainException.AlreadyExists("User already in team")
        val member = teamMemberRepository.add(
            TeamMember(
                id = UUID.randomUUID(),
                teamId = teamId,
                userId = userId,
                role = role,
                joinedAt = Clock.System.now(),
                invitedBy = requesterId,
                isActive = true,
                lastActiveAt = null
            )
        )

        return TeamMemberResponse.from(member)
    }

    override suspend fun removeMember(teamId: UUID, memberId: UUID, requesterId: UUID) {
        canManageTeam(teamId, requesterId)

        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        if(!teamMemberRepository.exists(memberId, requesterId)) throw DomainException.NotFound("User", requesterId.toString())
        teamMemberRepository.remove(memberId)
    }

    override suspend fun changeRole(
        teamId: UUID,
        memberId: UUID,
        role: TeamRole,
        requesterId: UUID
    ) {
        canManageTeam(teamId, requesterId)

        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        if(!teamMemberRepository.exists(memberId, requesterId)) throw DomainException.NotFound("User", requesterId.toString())
        teamMemberRepository.updateRole(memberId, role)
    }

}