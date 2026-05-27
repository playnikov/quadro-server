package com.quadro.notification.infrastructure.messaging.listener

import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.ProjectCreatedEvent
import com.quadro.shared.data.messaging.events.ProjectDeletedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberAddedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberRemovedEvent
import com.quadro.shared.data.messaging.events.ProjectMemberUpdatedRoleEvent
import com.quadro.shared.data.messaging.events.ProjectUpdatedEvent
import com.quadro.notification.infrastructure.messaging.processor.ProjectEventProcessor
import com.quadro.shared.data.messaging.events.ProjectInviteCreateEvent
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class ProjectEventListener : KoinComponent {
    private val json = Json { ignoreUnknownKeys = true }

    private val projectEventProcessor: ProjectEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject(named("projectConsumer"))

    fun start() {
        eventConsumer.start { topic, _, value ->
            when (topic) {
                KafkaTopics.PROJECT_CREATED -> {
                    val event = json.decodeFromString<ProjectCreatedEvent>(value)
                    projectEventProcessor.processProjectCreated(event)
                }
                KafkaTopics.PROJECT_UPDATED -> {
                    val event = json.decodeFromString<ProjectUpdatedEvent>(value)
                    projectEventProcessor.processProjectUpdated(event)
                }
                KafkaTopics.PROJECT_DELETED -> {
                    val event = json.decodeFromString<ProjectDeletedEvent>(value)
                    projectEventProcessor.processProjectDeleted(event)
                }

                KafkaTopics.PROJECT_MEMBER_ADDED -> {
                    val event = json.decodeFromString<ProjectMemberAddedEvent>(value)
                    projectEventProcessor.processMemberCreated(event)
                }

                KafkaTopics.PROJECT_MEMBER_ROLE_UPDATED -> {
                    val event = json.decodeFromString<ProjectMemberUpdatedRoleEvent>(value)
                    projectEventProcessor.processMemberUpdated(event)
                }

                KafkaTopics.PROJECT_MEMBER_REMOVED -> {
                    val event = json.decodeFromString<ProjectMemberRemovedEvent>(value)
                    projectEventProcessor.processMemberDeleted(event)
                }

                KafkaTopics.PROJECT_INVITED -> {
                    val event = json.decodeFromString<ProjectInviteCreateEvent>(value)
                    projectEventProcessor.processInvited(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}