package com.quadro.company.di

import com.quadro.company.infrastructure.messaging.UserEventListener
import com.quadro.company.infrastructure.messaging.UserEventProcessor
import com.quadro.shared.data.config.KafkaConfig
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import org.koin.dsl.module

val kafkaModule = module {
    single { EventProducer(get()) }
    single {
        EventConsumer(
            get<KafkaConfig>().bootstrapServers,
            get<KafkaConfig>().groupId,
            listOf(
                KafkaTopics.USER_CREATED,
                KafkaTopics.USER_UPDATED,
                KafkaTopics.USER_DEACTIVATED
            )
        )
    }
    single { UserEventListener() }
    single { UserEventProcessor(get()) }
}