package com.quadro.team.infrastructure.messaging.listener

import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.CompanyCreatedEvent
import com.quadro.shared.data.messaging.events.CompanyDeletedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberAddedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberRemovedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberRoleUpdatedEvent
import com.quadro.shared.data.messaging.events.CompanyUpdatedEvent
import com.quadro.team.infrastructure.messaging.processor.CompanyEventProcessor
import com.quadro.team.infrastructure.messaging.processor.CompanyMemberEventProcessor
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class CompanyMemberEventListener : KoinComponent {
    private val json = Json { ignoreUnknownKeys = true }

    private val companyMemberEventProcessor: CompanyMemberEventProcessor by inject()
    private val eventConsumer: EventConsumer by inject(named("memberConsumer"))

    fun start() {
        eventConsumer.start { topic, _, value ->
            when (topic) {
                KafkaTopics.COMPANY_MEMBER_ADDED -> {
                    val event = json.decodeFromString<CompanyMemberAddedEvent>(value)
                    companyMemberEventProcessor.processCreated(event)
                }
                KafkaTopics.COMPANY_MEMBER_ROLE_UPDATED -> {
                    val event = json.decodeFromString<CompanyMemberRoleUpdatedEvent>(value)
                    companyMemberEventProcessor.processUpdated(event)
                }
                KafkaTopics.COMPANY_MEMBER_REMOVED -> {
                    val event = json.decodeFromString<CompanyMemberRemovedEvent>(value)
                    companyMemberEventProcessor.processDeleted(event)
                }
            }
        }
    }

    suspend fun stop() {
        eventConsumer.stop()
    }
}