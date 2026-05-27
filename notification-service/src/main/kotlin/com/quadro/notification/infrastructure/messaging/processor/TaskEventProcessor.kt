package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.notification.infrastructure.websocket.WebSocketManager
import com.quadro.shared.data.messaging.events.TaskCreatedEvent
import com.quadro.shared.data.messaging.events.TaskUpdatedEvent
import com.quadro.shared.data.messaging.events.TaskAssignedEvent
import com.quadro.shared.data.messaging.events.TaskCommentEvent
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.koin.core.component.KoinComponent

class TaskEventProcessor : KoinComponent {
    private val webSocketManager = WebSocketManager


    suspend fun processCreated(event: TaskCreatedEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_CREATED")
            put("projectId", event.projectId)
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
            put("projectId", event.projectId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("title", event.title)
                put("updatedBy", event.updatedBy)
            }
        }.toString()
        webSocketManager.sendProjectNotification(event.projectId, notification)
    }

    suspend fun processAssigned(event: TaskAssignedEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_ASSIGNED")
            put("projectId", event.projectId)
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
            put("taskId", event.taskId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("commentId", event.commentId)
            }
        }.toString()
        webSocketManager.sendTaskNotification(event.taskId, notification)
    }

    suspend fun processCommentUpdate(event: TaskCommentEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_COMMENT_UPDATED")
            put("taskId", event.taskId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("commentId", event.commentId)
            }
        }.toString()
        webSocketManager.sendTaskNotification(event.taskId, notification)
    }

    suspend fun processCommentRemoved(event: TaskCommentEvent) {
        val notification = buildJsonObject {
            put("type", "TASK_COMMENT_REMOVED")
            put("taskId", event.taskId)
            putJsonObject("data") {
                put("taskId", event.taskId)
                put("commentId", event.commentId)
            }
        }.toString()
        webSocketManager.sendTaskNotification(event.taskId, notification)
    }
}