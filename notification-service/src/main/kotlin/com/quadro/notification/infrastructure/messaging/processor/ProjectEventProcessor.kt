package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberAddedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberRemovedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberUpdatedRoleEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class ProjectEventProcessor(

) : KoinComponent {

    suspend fun processProjectCreated(event: ProjectCreatedEvent) {
        val title = "Создан проект: ${event.name}"
        val message = "Создан новый проект \"${event.name}\""
    }

    suspend fun processProjectUpdated(event: ProjectUpdatedEvent) {
        val title = "Обновлен проект: ${event.name}"
        val message = "Проект \"${event.name}\" был обновлен"
    }

    suspend fun processProjectDeleted(event: ProjectDeletedEvent) {

    }

    suspend fun processMemberCreated(event: ProjectMemberAddedEvent) {

    }

    suspend fun processMemberUpdated(event: ProjectMemberUpdatedRoleEvent) {

    }

    suspend fun processMemberDeleted(event: ProjectMemberRemovedEvent) {

    }
}