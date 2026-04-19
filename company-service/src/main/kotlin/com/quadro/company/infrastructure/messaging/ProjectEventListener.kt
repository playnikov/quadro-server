package com.quadro.company.infrastructure.messaging

import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.ProjectArchivedEvent
import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory

class ProjectEventListener : KoinComponent {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true }

    private val projectEventProcessor: ProjectEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject(named("projectConsumer"))

    fun start() {
        eventConsumer.start { topic, key, value ->
            when (topic) {
                KafkaTopics.PROJECT_CREATED -> {
                    val event = json.decodeFromString<ProjectCreatedEvent>(value)
                    projectEventProcessor.processCreated(event)
                }
                KafkaTopics.PROJECT_DELETED -> {
                    val event = json.decodeFromString<ProjectDeletedEvent>(value)
                    projectEventProcessor.processDeleted(event)
                }
                KafkaTopics.PROJECT_ARCHIVED -> {
                    val event = json.decodeFromString<ProjectArchivedEvent>(value)
                    projectEventProcessor.processArchived(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}