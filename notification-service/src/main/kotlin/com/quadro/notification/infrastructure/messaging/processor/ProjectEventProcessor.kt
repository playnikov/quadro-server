package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.notification.infrastructure.websocket.WebSocketManager
import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectInviteCreateEvent
import com.quadro.shared.data.messaging.events.ProjectMemberAddedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberRemovedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberUpdatedRoleEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

class ProjectEventProcessor(

) : KoinComponent {
    private val webSocketManager = WebSocketManager

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

    suspend fun processInvited(event: ProjectInviteCreateEvent) {
        event.userId?.let { userId ->
            val notification = buildJsonObject {
                put("type", "INVITED")
                put("userId", userId)
                putJsonObject("data") {
                    put("projectName", event.projectName)
                }
            }.toString()

            webSocketManager.sendNotification(userId, notification)
        }
    }
}