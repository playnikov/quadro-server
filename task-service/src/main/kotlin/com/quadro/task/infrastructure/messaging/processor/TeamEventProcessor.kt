package com.quadro.task.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.TeamCreatedEvent
import com.quadro.shared.data.messaging.events.TeamDeletedEvent
import com.quadro.shared.data.messaging.events.TeamMemberAddedEvent
import com.quadro.shared.data.messaging.events.TeamMemberRemovedEvent
import com.quadro.shared.data.messaging.events.TeamMemberUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamProjectAssignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUnassignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamUpdatedEvent
import com.quadro.task.domain.models.team.Team
import com.quadro.task.domain.models.team.TeamMember
import com.quadro.task.domain.models.team.TeamProject
import com.quadro.task.domain.models.team.TeamProjectRole
import com.quadro.task.domain.models.team.TeamRole
import com.quadro.task.domain.models.team.TeamStatus
import com.quadro.task.domain.repositories.task.TaskRepository
import com.quadro.task.domain.repositories.team.TeamMemberRepository
import com.quadro.task.domain.repositories.team.TeamProjectRepository
import com.quadro.task.domain.repositories.team.TeamRepository
import java.util.UUID

class TeamEventProcessor(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamProjectRepository: TeamProjectRepository,
    private val taskRepository: TaskRepository
) {
    suspend fun processTeamCreated(event: TeamCreatedEvent) {
        val team = Team(
            id = UUID.fromString(event.teamId),
            status = TeamStatus.valueOf(event.status)
        )

        teamRepository.upsert(team)
    }

    suspend fun processTeamUpdated(event: TeamUpdatedEvent) {
        val team = Team(
            id = UUID.fromString(event.teamId),
            status = TeamStatus.valueOf(event.status)
        )

        teamRepository.upsert(team)
    }

    suspend fun processTeamDeleted(event: TeamDeletedEvent) {
        taskRepository.clearAssignedTeam(UUID.fromString(event.teamId))
        teamMemberRepository.deleteByTeam(UUID.fromString(event.teamId))
        teamRepository.delete(UUID.fromString(event.teamId))
    }

    suspend fun processTeamMemberAdded(event: TeamMemberAddedEvent) {
        val member = TeamMember(
            teamId = UUID.fromString(event.teamId),
            userId = UUID.fromString(event.userId),
            role = TeamRole.valueOf(event.role),
            isActive = event.isActive
        )

        teamMemberRepository.upsert(member)
    }

    suspend fun processTeamMemberUpdated(event: TeamMemberUpdatedEvent) {
        val member = TeamMember(
            teamId = UUID.fromString(event.teamId),
            userId = UUID.fromString(event.userId),
            role = TeamRole.valueOf(event.role),
            isActive = event.isActive
        )

        teamMemberRepository.upsert(member)
    }

    suspend fun processTeamMemberRemoved(event: TeamMemberRemovedEvent) {
        taskRepository.clearAssignee(UUID.fromString(event.userId))
        teamMemberRepository.delete(UUID.fromString(event.teamId), UUID.fromString(event.userId))
    }

    suspend fun processTeamProjectAssigned(event: TeamProjectAssignedEvent) {
        val teamProject = TeamProject(
            teamId = UUID.fromString(event.teamId),
            projectId = UUID.fromString(event.projectId),
            role = TeamProjectRole.valueOf(event.role)
        )

        teamProjectRepository.upsert(teamProject)
    }

    suspend fun processTeamProjectUpdated(event: TeamProjectUpdatedEvent) {
        val teamProject = TeamProject(
            teamId = UUID.fromString(event.teamId),
            projectId = UUID.fromString(event.projectId),
            role = TeamProjectRole.valueOf(event.role)
        )

        teamProjectRepository.upsert(teamProject)
    }

    suspend fun processTeamProjectUnassigned(event: TeamProjectUnassignedEvent) {
        teamProjectRepository.delete(UUID.fromString(event.teamId), UUID.fromString(event.projectId))
    }
}