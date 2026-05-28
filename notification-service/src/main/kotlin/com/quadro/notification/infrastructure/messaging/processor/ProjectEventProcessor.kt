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
        val notification = buildJsonObject {
            put("type", "PROJECT_CREATED")
            put("projectId", event.projectId)
            putJsonObject("data") {
                put("name", event.name)
                put("key", event.key)
                put("ownerId", event.ownerId)
            }
        }.toString()

        webSocketManager.sendNotification(event.ownerId, notification)
    }

    suspend fun processProjectUpdated(event: ProjectUpdatedEvent) {
        val notification = buildJsonObject {
            put("type", "PROJECT_UPDATED")
            put("projectId", event.projectId)
            putJsonObject("data") {
                put("name", event.name)
                put("key", event.key)
                put("status", event.status)
                put("updatedBy", event.updateBy)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
    }

    suspend fun processProjectDeleted(event: ProjectDeletedEvent) {
        val notification = buildJsonObject {
            put("type", "PROJECT_DELETED")
            put("projectId", event.projectId)
            putJsonObject("data") {
                put("deletedBy", event.deletedBy)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
    }

    suspend fun processMemberCreated(event: ProjectMemberAddedEvent) {
        val notification = buildJsonObject {
            put("type", "PROJECT_MEMBER_ADDED")
            put("projectId", event.projectId)
            putJsonObject("data") {
                put("userId", event.userId)
                put("role", event.role)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
        webSocketManager.sendNotification(event.userId, notification)
    }

    suspend fun processMemberUpdated(event: ProjectMemberUpdatedRoleEvent) {
        val notification = buildJsonObject {
            put("type", "PROJECT_MEMBER_UPDATED")
            put("projectId", event.projectId)
            putJsonObject("data") {
                put("userId", event.userId)
                put("role", event.role)
            }
        }.toString()
        webSocketManager.sendNotification(event.userId, notification)
        webSocketManager.sendProjectNotification(event.projectId, notification)
    }

    suspend fun processMemberDeleted(event: ProjectMemberRemovedEvent) {
        val notification = buildJsonObject {
            put("type", "PROJECT_MEMBER_REMOVED")
            put("projectId", event.projectId)
            putJsonObject("data") {
                put("userId", event.userId)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
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