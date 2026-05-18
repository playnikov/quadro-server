package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.TeamCreatedEvent
import com.quadro.shared.data.messaging.events.TeamDeletedEvent
import com.quadro.shared.data.messaging.events.TeamMemberAddedEvent
import com.quadro.shared.data.messaging.events.TeamMemberRemovedEvent
import com.quadro.shared.data.messaging.events.TeamMemberUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamProjectAssignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUnassignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamUpdatedEvent
import com.quadro.notification.domain.models.team.Team
import com.quadro.notification.domain.models.team.TeamMember
import com.quadro.notification.domain.models.team.TeamProject
import com.quadro.notification.domain.models.team.TeamProjectRole
import com.quadro.notification.domain.models.team.TeamRole
import com.quadro.notification.domain.models.team.TeamStatus
import com.quadro.notification.domain.repositories.task.TaskRepository
import com.quadro.notification.domain.repositories.team.TeamMemberRepository
import com.quadro.notification.domain.repositories.team.TeamProjectRepository
import com.quadro.notification.domain.repositories.team.TeamRepository
import com.quadro.notification.domain.services.NotificationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class TeamEventProcessor(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamProjectRepository: TeamProjectRepository,
    private val taskRepository: TaskRepository
) : KoinComponent {
    private val notificationService: NotificationService by inject()

    suspend fun processTeamCreated(event: TeamCreatedEvent) {
        val team = Team(
            id = UUID.fromString(event.teamId),
            status = TeamStatus.valueOf(event.status)
        )

        teamRepository.upsert(team)
        notificationService.sendNotification(event)
    }

    suspend fun processTeamUpdated(event: TeamUpdatedEvent) {
        val team = Team(
            id = UUID.fromString(event.teamId),
            status = TeamStatus.valueOf(event.status)
        )

        teamRepository.upsert(team)
        notificationService.sendNotification(event)
    }

    suspend fun processTeamDeleted(event: TeamDeletedEvent) {
        taskRepository.clearAssignedTeam(UUID.fromString(event.teamId))
        teamMemberRepository.deleteByTeam(UUID.fromString(event.teamId))
        teamRepository.delete(UUID.fromString(event.teamId))
        notificationService.sendNotification(event)
    }

    suspend fun processTeamMemberAdded(event: TeamMemberAddedEvent) {
        val member = TeamMember(
            teamId = UUID.fromString(event.teamId),
            userId = UUID.fromString(event.userId),
            role = TeamRole.valueOf(event.role),
            isActive = event.isActive
        )

        teamMemberRepository.upsert(member)
        notificationService.sendNotification(event)
    }

    suspend fun processTeamMemberUpdated(event: TeamMemberUpdatedEvent) {
        val member = TeamMember(
            teamId = UUID.fromString(event.teamId),
            userId = UUID.fromString(event.userId),
            role = TeamRole.valueOf(event.role),
            isActive = event.isActive
        )

        teamMemberRepository.upsert(member)
        notificationService.sendNotification(event)
    }

    suspend fun processTeamMemberRemoved(event: TeamMemberRemovedEvent) {
        taskRepository.clearAssignee(UUID.fromString(event.userId))
        teamMemberRepository.delete(UUID.fromString(event.teamId), UUID.fromString(event.userId))
        notificationService.sendNotification(event)
    }

    suspend fun processTeamProjectAssigned(event: TeamProjectAssignedEvent) {
        val teamProject = TeamProject(
            teamId = UUID.fromString(event.teamId),
            projectId = UUID.fromString(event.projectId),
            role = TeamProjectRole.valueOf(event.role)
        )

        teamProjectRepository.upsert(teamProject)
        notificationService.sendNotification(event)
    }

    suspend fun processTeamProjectUpdated(event: TeamProjectUpdatedEvent) {
        val teamProject = TeamProject(
            teamId = UUID.fromString(event.teamId),
            projectId = UUID.fromString(event.projectId),
            role = TeamProjectRole.valueOf(event.role)
        )

        teamProjectRepository.upsert(teamProject)
        notificationService.sendNotification(event)
    }

    suspend fun processTeamProjectUnassigned(event: TeamProjectUnassignedEvent) {
        teamProjectRepository.delete(UUID.fromString(event.teamId), UUID.fromString(event.projectId))
        notificationService.sendNotification(event)
    }
}