package com.quadro.project.infrastructure.messaging.listener

import com.quadro.project.infrastructure.messaging.processor.CompanyEventProcessor
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.CompanyCreatedEvent
import com.quadro.shared.data.messaging.events.CompanyDeletedEvent
import com.quadro.shared.data.messaging.events.CompanyUpdatedEvent
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.LoggerFactory

class CompanyEventListener : KoinComponent {
    private val json = Json { ignoreUnknownKeys = true }

    private val companyEventProcessor: CompanyEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject(named("companyConsumer"))

    fun start() {
        eventConsumer.start { topic, _, value ->
            when (topic) {
                KafkaTopics.COMPANY_CREATED -> {
                    val event = json.decodeFromString<CompanyCreatedEvent>(value)
                    companyEventProcessor.processCreated(event)
                }
                KafkaTopics.COMPANY_UPDATED -> {
                    val event = json.decodeFromString<CompanyUpdatedEvent>(value)
                    companyEventProcessor.processUpdated(event)
                }
                KafkaTopics.COMPANY_DELETED -> {
                    val event = json.decodeFromString<CompanyDeletedEvent>(value)
                    companyEventProcessor.processDeleted(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}