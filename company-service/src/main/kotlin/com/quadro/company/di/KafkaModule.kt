package com.quadro.company.di

import com.quadro.company.config.KafkaConfig
import com.quadro.company.domain.services.EventPublisher
import com.quadro.company.domain.services.EventPublisherImpl
import com.quadro.company.infrastructure.messaging.UserEventProcessor
import com.quadro.shared.kafka.KafkaConsumerService
import com.quadro.shared.events.UserEvent
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.dsl.module
import java.util.Properties

val kafkaModule = module {
    single {
        createProducer(get<KafkaConfig>().brokers)
    }

    single<EventPublisher> { EventPublisherImpl(get()) }

    single { UserEventProcessor(get()) }

    single {
        val config = get<KafkaConfig>()
        val processor = get<UserEventProcessor>()
        val json = Json { ignoreUnknownKeys = true }

        KafkaConsumerService(
            bootstrapServers = config.brokers,
            groupId = config.groupId,
            topics = listOf("user-events", "project-events"),
            onMessage = { raw ->
                val event = json.decodeFromString<UserEvent>(raw)
                when (event) {
                    is UserEvent.Created -> processor.processCreated(event)
                    is UserEvent.Updated -> processor.processUpdated(event)
                    is UserEvent.Deleted -> processor.processDeleted(event)
                }
            }
        )
    }
}

private fun createProducer(bootstrapServers: String): KafkaProducer<String, String> {
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.CLIENT_ID_CONFIG, "company-producer")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.RETRIES_CONFIG, 3)
    }
    return KafkaProducer(props)
}