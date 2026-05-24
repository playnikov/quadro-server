package com.quadro.task.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberAddedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberRemovedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberUpdatedRoleEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import com.quadro.task.domain.models.project.Project
import com.quadro.task.domain.models.project.ProjectMember
import com.quadro.task.domain.models.project.MemberRole
import com.quadro.task.domain.models.project.ProjectStatus
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.domain.repositories.project.ProjectRepository
import java.util.UUID

class ProjectEventProcessor(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository
) {
    suspend fun processProjectCreated(event: ProjectCreatedEvent) {
        val project = Project(
            id = UUID.fromString(event.projectId),
            key = event.key,
            status = ProjectStatus.valueOf(event.status)
        )

        projectRepository.upsert(project)
    }

    suspend fun processProjectUpdated(event: ProjectUpdatedEvent) {
        val project = Project(
            id = UUID.fromString(event.projectId),
            key = event.key,
            status = ProjectStatus.valueOf(event.status)
        )

        projectRepository.upsert(project)
    }

    suspend fun processProjectDeleted(event: ProjectDeletedEvent) {
        projectMemberRepository.deleteByProject(UUID.fromString(event.projectId))
        projectRepository.delete(UUID.fromString(event.projectId))
    }

    suspend fun processMemberCreated(event: ProjectMemberAddedEvent) {
        val member = ProjectMember(
            projectId = UUID.fromString(event.projectId),
            userId = UUID.fromString(event.userId),
            role = MemberRole.valueOf(event.role)
        )

        projectMemberRepository.upsert(member)
    }

    suspend fun processMemberUpdated(event: ProjectMemberUpdatedRoleEvent) {
        val member = ProjectMember(
            projectId = UUID.fromString(event.projectId),
            userId = UUID.fromString(event.userId),
            role = MemberRole.valueOf(event.role)
        )

        projectMemberRepository.upsert(member)
    }

    suspend fun processMemberDeleted(event: ProjectMemberRemovedEvent) {
        projectMemberRepository.delete(UUID.fromString(event.projectId), UUID.fromString(event.userId))
    }
}