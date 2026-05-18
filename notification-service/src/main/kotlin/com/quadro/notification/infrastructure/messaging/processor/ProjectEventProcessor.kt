package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberAddedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberRemovedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberUpdatedRoleEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import com.quadro.notification.domain.models.project.Project
import com.quadro.notification.domain.models.project.ProjectMember
import com.quadro.notification.domain.models.project.ProjectRole
import com.quadro.notification.domain.models.project.ProjectStatus
import com.quadro.notification.domain.repositories.project.ProjectMemberRepository
import com.quadro.notification.domain.repositories.project.ProjectRepository
import com.quadro.notification.domain.services.NotificationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class ProjectEventProcessor(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository
) : KoinComponent {
    private val notificationService: NotificationService by inject()

    suspend fun processProjectCreated(event: ProjectCreatedEvent) {
        val project = Project(
            id = UUID.fromString(event.projectId),
            key = event.key,
            status = ProjectStatus.valueOf(event.status)
        )

        projectRepository.upsert(project)
        notificationService.sendNotification(event)
    }

    suspend fun processProjectUpdated(event: ProjectUpdatedEvent) {
        val project = Project(
            id = UUID.fromString(event.projectId),
            key = event.key,
            status = ProjectStatus.valueOf(event.status)
        )

        projectRepository.upsert(project)
        notificationService.sendNotification(event)
    }

    suspend fun processProjectDeleted(event: ProjectDeletedEvent) {
        projectMemberRepository.deleteByProject(UUID.fromString(event.projectId))
        projectRepository.delete(UUID.fromString(event.projectId))
        notificationService.sendNotification(event)
    }

    suspend fun processMemberCreated(event: ProjectMemberAddedEvent) {
        val member = ProjectMember(
            projectId = UUID.fromString(event.projectId),
            userId = UUID.fromString(event.userId),
            role = ProjectRole.valueOf(event.role)
        )

        projectMemberRepository.upsert(member)
        notificationService.sendNotification(event)
    }

    suspend fun processMemberUpdated(event: ProjectMemberUpdatedRoleEvent) {
        val member = ProjectMember(
            projectId = UUID.fromString(event.projectId),
            userId = UUID.fromString(event.userId),
            role = ProjectRole.valueOf(event.role)
        )

        projectMemberRepository.upsert(member)
        notificationService.sendNotification(event)
    }

    suspend fun processMemberDeleted(event: ProjectMemberRemovedEvent) {
        projectMemberRepository.delete(UUID.fromString(event.projectId), UUID.fromString(event.userId))
        notificationService.sendNotification(event)
    }
}