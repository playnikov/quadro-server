package com.quadro.team.infrastructure.messaging.listener

import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import com.quadro.team.infrastructure.messaging.processor.ProjectEventProcessor
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory

class ProjectEventListener : KoinComponent {
    private val json = Json { ignoreUnknownKeys = true }

    private val projectEventProcessor: ProjectEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject(named("projectConsumer"))

    fun start() {
        eventConsumer.start { topic, _, value ->
            when (topic) {
                KafkaTopics.PROJECT_CREATED -> {
                    val event = json.decodeFromString<ProjectCreatedEvent>(value)
                    projectEventProcessor.processCreated(event)
                }
                KafkaTopics.PROJECT_UPDATED -> {
                    val event = json.decodeFromString<ProjectUpdatedEvent>(value)
                    projectEventProcessor.processUpdated(event)
                }
                KafkaTopics.PROJECT_DELETED -> {
                    val event = json.decodeFromString<ProjectDeletedEvent>(value)
                    projectEventProcessor.processDeleted(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}