package com.quadro.team.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamMemberResponse
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.repositories.CompanyMemberRepository
import com.quadro.team.domain.repositories.TeamMemberRepository
import com.quadro.team.domain.repositories.TeamRepository
import java.util.UUID
import kotlin.time.Clock

class TeamMemberServiceImpl(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val companyMemberRepository: CompanyMemberRepository
) : TeamMemberService {
    override suspend fun getMembers(teamId: UUID): List<TeamMemberResponse> {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        return teamMemberRepository.findByTeam(teamId).map { TeamMemberResponse.from(it) }
    }

    override suspend fun addMember(
        teamId: UUID,
        userId: UUID,
        role: TeamRole,
        addedBy: UUID
    ): TeamMemberResponse {
        val team = teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        companyMemberRepository.findByCompanyAndUser(team.companyId, userId)
            ?: throw DomainException.NotFound("Member", "Member with id $team.companyId not found")

        if (teamMemberRepository.exists(teamId, userId)) throw DomainException.AlreadyExists("User already in team")
        val member = teamMemberRepository.add(
            TeamMember(
                id = UUID.randomUUID(),
                teamId = teamId,
                userId = userId,
                role = role,
                joinedAt = Clock.System.now(),
                invitedBy = addedBy,
                isActive = true,
                lastActiveAt = null
            )
        )

        return TeamMemberResponse.from(member)
    }

    override suspend fun removeMember(teamId: UUID, memberId: UUID, requesterId: UUID) {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        if(!teamMemberRepository.exists(memberId, requesterId)) throw DomainException.NotFound("User", "User does not exist")
        teamMemberRepository.remove(memberId)
    }

    override suspend fun changeRole(
        teamId: UUID,
        memberId: UUID,
        role: TeamRole,
        requesterId: UUID
    ) {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        if(!teamMemberRepository.exists(memberId, requesterId)) throw DomainException.NotFound("User", "User does not exist")
        teamMemberRepository.updateRole(memberId, role)
    }

}