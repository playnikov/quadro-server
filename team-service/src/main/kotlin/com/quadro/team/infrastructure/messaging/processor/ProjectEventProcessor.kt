package com.quadro.team.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import com.quadro.team.domain.models.Project
import com.quadro.team.domain.models.ProjectStatus
import com.quadro.team.domain.repositories.ProjectRepository
import java.time.Instant
import java.util.UUID
import kotlin.time.toKotlinInstant

class ProjectEventProcessor(
    private val projectRepository: ProjectRepository
) {
    suspend fun processCreated(event: ProjectCreatedEvent) {
        val project = Project(
            id = UUID.fromString(event.projectId),
            status = ProjectStatus.valueOf(event.status),
            updatedAt = Instant.ofEpochMilli(event.updatedAt).toKotlinInstant()
        )

        projectRepository.upsert(project)
    }

    suspend fun processUpdated(event: ProjectUpdatedEvent) {
        val project = Project(
            id = UUID.fromString(event.projectId),
            status = ProjectStatus.valueOf(event.status),
            updatedAt = Instant.ofEpochMilli(event.updatedAt).toKotlinInstant()
        )

        projectRepository.upsert(project)
    }

    suspend fun processDeleted(event: ProjectDeletedEvent) {
        projectRepository.delete(UUID.fromString(event.projectId))
    }
}