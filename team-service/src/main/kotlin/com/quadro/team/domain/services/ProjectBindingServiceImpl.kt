package com.quadro.team.domain.services

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TeamProjectAssignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUnassignedEvent
import com.quadro.shared.dto.DomainException
import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.models.TeamProjectBindingResponse
import com.quadro.team.domain.models.TeamProjectRole
import com.quadro.team.domain.repositories.ProjectRepository
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.domain.repositories.TeamRepository
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class ProjectBindingServiceImpl(
    private val teamRepository: TeamRepository,
    private val projectRepository: ProjectRepository,
    private val bindingRepository: TeamProjectBindingRepository,
    private val eventProducer: EventProducer
) : ProjectBindingService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private suspend fun checkTeamExists(teamId: UUID): Team =
        teamRepository.findById(teamId)
            ?: throw DomainException.NotFound("Team", teamId.toString())

    override suspend fun bind(
        teamId: UUID,
        projectId: UUID,
        role: TeamProjectRole,
        requesterId: UUID
    ): TeamProjectBindingResponse {
        checkTeamExists(teamId)
        projectRepository.findById(projectId) ?: throw DomainException.NotFound("Project", projectId.toString())
        if (bindingRepository.exists(teamId, projectId)) throw DomainException.AlreadyExists("Binding")
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

        eventProducer.publish(
            topic = KafkaTopics.TEAM_PROJECT_ASSIGNED,
            key = bind.id.toString(),
            event = TeamProjectAssignedEvent(
                teamId = bind.teamId.toString(),
                projectId = bind.projectId.toString(),
                role = bind.role.name
            )
        )

        return TeamProjectBindingResponse.from(bind)
    }

    override suspend fun unbind(teamId: UUID, projectId: UUID, requesterId: UUID) {
        checkTeamExists(teamId)
        projectRepository.findById(projectId) ?: throw DomainException.NotFound("Project", projectId.toString())
        bindingRepository.unbind(teamId, projectId)

        eventProducer.publish(
            topic = KafkaTopics.TEAM_PROJECT_UNASSIGNED,
            key = projectId.toString(),
            event = TeamProjectUnassignedEvent(
                teamId = teamId.toString(),
                projectId = projectId.toString()
            )
        )
    }

    override suspend fun getBindingsByTeam(teamId: UUID): List<TeamProjectBindingResponse> {
        checkTeamExists(teamId)
        return bindingRepository.findByTeam(teamId).map { TeamProjectBindingResponse.from(it) }
    }
}