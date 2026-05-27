package com.quadro.notification.infrastructure.messaging.listener

import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TaskCreatedEvent
import com.quadro.shared.data.messaging.events.TaskUpdatedEvent
import com.quadro.shared.data.messaging.events.TaskAssignedEvent
import com.quadro.notification.infrastructure.messaging.processor.TaskEventProcessor
import com.quadro.shared.data.messaging.events.TaskCommentEvent
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class TaskEventListener : KoinComponent {
    private val json = Json { ignoreUnknownKeys = true }

    private val taskEventProcessor: TaskEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject(named("taskConsumer"))

    fun start() {
        eventConsumer.start { topic, key, value ->
            when (topic) {
                KafkaTopics.TASK_CREATED -> {
                    val event = json.decodeFromString<TaskCreatedEvent>(value)
                    taskEventProcessor.processCreated(event)
                }
                
                KafkaTopics.TASK_UPDATED -> {
                    val event = json.decodeFromString<TaskUpdatedEvent>(value)
                    taskEventProcessor.processUpdated(event)
                }
                
                KafkaTopics.TASK_ASSIGNED -> {
                    val event = json.decodeFromString<TaskAssignedEvent>(value)
                    taskEventProcessor.processAssigned(event)
                }
                
                KafkaTopics.TASK_COMMENT_ADD -> {
                    val event = json.decodeFromString<TaskCommentEvent>(value)
                    taskEventProcessor.processCommented(event)
                }

                KafkaTopics.TASK_COMMENT_UPDATED -> {
                    val event = json.decodeFromString<TaskCommentEvent>(value)
                    taskEventProcessor.processCommentUpdate(event)
                }

                KafkaTopics.TASK_COMMENT_REMOVED -> {
                    val event = json.decodeFromString<TaskCommentEvent>(value)
                    taskEventProcessor.processCommentRemoved(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}