package com.quadro.team.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.models.TeamProjectBindingResponse
import com.quadro.team.domain.models.TeamProjectRole
import com.quadro.team.domain.repositories.CompanyMemberRepository
import com.quadro.team.domain.repositories.CompanyRepository
import com.quadro.team.domain.repositories.ProjectRepository
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.domain.repositories.TeamRepository
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class ProjectBindingServiceImpl(
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val teamRepository: TeamRepository,
    private val projectRepository: ProjectRepository,
    private val bindingRepository: TeamProjectBindingRepository
) : ProjectBindingService {
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

    override suspend fun bind(
        teamId: UUID,
        projectId: UUID,
        role: TeamProjectRole,
        requesterId: UUID
    ): TeamProjectBindingResponse {
        canManageTeam(teamId, requesterId)

        projectRepository.findById(projectId) ?: throw DomainException.NotFound("Project", projectId.toString())
        if (bindingRepository.exists(teamId, projectId)) throw DomainException.AlreadyExists("Binding already exists")
        val bind = bindingRepository.bind(
            TeamProjectBinding(
                id = UUID.randomUUID(),
                teamId = teamId,
                projectId = projectId,
                role = role,
                boundAt = Clock.System.now(),
                boundBy = requesterId
            )
        )
        return TeamProjectBindingResponse.from(bind)
    }

    override suspend fun unbind(teamId: UUID, projectId: UUID, requesterId: UUID) {
        canManageTeam(teamId, requesterId)
        projectRepository.findById(projectId) ?: throw DomainException.NotFound("Project", projectId.toString())
        bindingRepository.unbind(teamId, projectId)
    }

    override suspend fun getBindingsByTeam(teamId: UUID): List<TeamProjectBindingResponse> {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        return bindingRepository.findByTeam(teamId).map { TeamProjectBindingResponse.from(it) }
    }
}