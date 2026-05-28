package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.notification.infrastructure.websocket.WebSocketManager
import com.quadro.shared.data.messaging.events.TaskCreatedEvent
import com.quadro.shared.data.messaging.events.TaskUpdatedEvent
import com.quadro.shared.data.messaging.events.TaskAssignedEvent
import com.quadro.shared.data.messaging.events.TaskCommentEvent
import com.quadro.shared.data.messaging.events.TaskDeletedEvent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.koin.core.component.KoinComponent

class TaskEventProcessor : KoinComponent {
    private val webSocketManager = WebSocketManager


    suspend fun processCreated(event: TaskCreatedEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_CREATED")
            put("id", event.projectId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("title", event.title)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
    }

    suspend fun processUpdated(event: TaskUpdatedEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_UPDATED")
            put("id", event.projectId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("title", event.title)
                put("updatedBy", event.updatedBy)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
    }

    suspend fun processDeleted(event: TaskDeletedEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_DELETED")
            put("id", event.projectId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("title", event.title)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
    }

    suspend fun processAssigned(event: TaskAssignedEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_ASSIGNED")
            put("id", event.projectId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("title", event.title)
                put("assigneeId", event.assigneeId)
            }
        }.toString()
        webSocketManager.sendNotification(event.assigneeId, notification)
    }

    suspend fun processCommented(event: TaskCommentEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_COMMENTED")
            put("id", event.taskId)
            putJsonObject("data") {
                put("commentId", event.commentId)
            }
        }.toString()
        webSocketManager.sendTaskNotification(event.taskId, notification)
    }

    suspend fun processCommentUpdate(event: TaskCommentEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_COMMENT_UPDATED")
            put("id", event.taskId)
            putJsonObject("data") {
                put("commentId", event.commentId)
            }
        }.toString()
        webSocketManager.sendTaskNotification(event.taskId, notification)
    }

    suspend fun processCommentRemoved(event: TaskCommentEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_COMMENT_REMOVED")
            put("id", event.taskId)
            putJsonObject("data") {
                put("commentId", event.commentId)
            }
        }.toString()
        webSocketManager.sendTaskNotification(event.taskId, notification)
    }
}