package com.quadro.company.infrastructure.messaging

import com.quadro.shared.data.messaging.events.UserCreatedEvent
import com.quadro.shared.data.messaging.events.UserUpdatedEvent
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

class UserEventListener : KoinComponent {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true }

    private val userEventProcessor: UserEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject()

    fun start() {
        eventConsumer.start { topic, key, value ->
            when (topic) {
                KafkaTopics.USER_CREATED -> {
                    val event = json.decodeFromString<UserCreatedEvent>(value)
                    userEventProcessor.processCreated(event)
                }
                KafkaTopics.USER_UPDATED -> {
                    val event = json.decodeFromString<UserUpdatedEvent>(value)
                    userEventProcessor.processUpdated(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}