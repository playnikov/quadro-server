package com.quadro.team.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.models.TeamProjectBindingResponse
import com.quadro.team.domain.models.TeamProjectRole
import com.quadro.team.domain.repositories.ProjectRepository
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.domain.repositories.TeamRepository
import java.util.UUID
import kotlin.time.Clock

class ProjectBindingServiceImpl(
    private val teamRepository: TeamRepository,
    private val projectRepository: ProjectRepository,
    private val bindingRepository: TeamProjectBindingRepository
) : ProjectBindingService {
    override suspend fun bind(
        teamId: UUID,
        projectId: UUID,
        role: TeamProjectRole,
        boundBy: UUID
    ): TeamProjectBindingResponse {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        projectRepository.findById(projectId) ?: throw DomainException.NotFound("Project", projectId.toString())
        if (bindingRepository.exists(teamId, projectId)) throw DomainException.AlreadyExists("Binding already exists")
        val bind = bindingRepository.bind(
            TeamProjectBinding(
                id = UUID.randomUUID(),
                teamId = teamId,
                projectId = projectId,
                role = role,
                boundAt = Clock.System.now(),
                boundBy = boundBy
            )
        )
        return TeamProjectBindingResponse.from(bind)
    }

    override suspend fun unbind(teamId: UUID, projectId: UUID, requesterId: UUID) {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        projectRepository.findById(projectId) ?: throw DomainException.NotFound("Project", projectId.toString())
        bindingRepository.unbind(teamId, projectId)
    }

    override suspend fun getBindingsByTeam(teamId: UUID): List<TeamProjectBindingResponse> {
        teamRepository.findById(teamId) ?: throw DomainException.NotFound("Team", teamId.toString())
        return bindingRepository.findByTeam(teamId).map { TeamProjectBindingResponse.from(it) }
    }
}