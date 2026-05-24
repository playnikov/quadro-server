package com.quadro.notification.infrastructure.messaging.processor

import com.quadro.notification.infrastructure.websocket.WebSocketManager
import com.quadro.shared.data.messaging.events.TaskCreatedEvent
import com.quadro.shared.data.messaging.events.TaskUpdatedEvent
import com.quadro.shared.data.messaging.events.TaskAssignedEvent
import com.quadro.shared.data.messaging.events.TaskCommentedEvent
import org.koin.core.component.KoinComponent

class TaskEventProcessor : KoinComponent {
    private val webSocketManager = WebSocketManager

    suspend fun processCreated(event: TaskCreatedEvent) {
        val title = "Создана задача: ${event.title}"
        val message = "Создана новая задача \"${event.title}\" в проекте ${event.projectId}"
    }

    suspend fun processUpdated(event: TaskUpdatedEvent) {
    }

    suspend fun processAssigned(event: TaskAssignedEvent) {
        val title = "Назначение на задачу"
        val message = "Вас назначили на задачу в проекте ${event.projectId}"
        
        webSocketManager.sendNotification(event.assigneeId, message)
    }

    suspend fun processCommented(event: TaskCommentedEvent) {

    }
}