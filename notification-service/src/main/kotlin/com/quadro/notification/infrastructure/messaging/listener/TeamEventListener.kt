package com.quadro.notification.infrastructure.messaging.listener

import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.TeamCreatedEvent
import com.quadro.shared.data.messaging.events.TeamDeletedEvent
import com.quadro.shared.data.messaging.events.TeamMemberAddedEvent
import com.quadro.shared.data.messaging.events.TeamMemberRemovedEvent
import com.quadro.shared.data.messaging.events.TeamMemberUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamProjectAssignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUnassignedEvent
import com.quadro.shared.data.messaging.events.TeamProjectUpdatedEvent
import com.quadro.shared.data.messaging.events.TeamUpdatedEvent
import com.quadro.notification.infrastructure.messaging.processor.TeamEventProcessor
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class TeamEventListener : KoinComponent {
    private val json = Json { ignoreUnknownKeys = true }

    private val teamEventProcessor: TeamEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject(named("teamConsumer"))

    fun start() {
        eventConsumer.start { topic, key, value ->
            when (topic) {
                KafkaTopics.TEAM_CREATED -> {
                    val event = json.decodeFromString<TeamCreatedEvent>(value)
                    teamEventProcessor.processTeamCreated(event)
                }

                KafkaTopics.TEAM_UPDATED -> {
                    val event = json.decodeFromString<TeamUpdatedEvent>(value)
                    teamEventProcessor.processTeamUpdated(event)
                }

                KafkaTopics.TEAM_DELETED -> {
                    val event = json.decodeFromString<TeamDeletedEvent>(value)
                    teamEventProcessor.processTeamDeleted(event)
                }

                KafkaTopics.TEAM_MEMBER_ADDED -> {
                    val event = json.decodeFromString<TeamMemberAddedEvent>(value)
                    teamEventProcessor.processTeamMemberAdded(event)
                }

                KafkaTopics.TEAM_MEMBER_UPDATED -> {
                    val event = json.decodeFromString<TeamMemberUpdatedEvent>(value)
                    teamEventProcessor.processTeamMemberUpdated(event)
                }

                KafkaTopics.TEAM_MEMBER_REMOVED -> {
                    val event = json.decodeFromString<TeamMemberRemovedEvent>(value)
                    teamEventProcessor.processTeamMemberRemoved(event)
                }

                KafkaTopics.TEAM_PROJECT_ASSIGNED -> {
                    val event = json.decodeFromString<TeamProjectAssignedEvent>(value)
                    teamEventProcessor.processTeamProjectAssigned(event)
                }


                KafkaTopics.TEAM_PROJECT_UPDATED -> {
                    val event = json.decodeFromString<TeamProjectUpdatedEvent>(value)
                    teamEventProcessor.processTeamProjectUpdated(event)
                }

                KafkaTopics.TEAM_PROJECT_UNASSIGNED -> {
                    val event = json.decodeFromString<TeamProjectUnassignedEvent>(value)
                    teamEventProcessor.processTeamProjectUnassigned(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}