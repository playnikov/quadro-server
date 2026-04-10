package com.quadro.company.di

import com.quadro.company.config.KafkaConfig
import com.quadro.company.infrastructure.messaging.UserEventListener
import com.quadro.shared.kafka.EventConsumer
import com.quadro.shared.kafka.EventProducer
import com.quadro.shared.kafka.KafkaTopics
import org.koin.dsl.module

val kafkaModule = module {
    single { EventProducer(get<KafkaConfig>().brokers) }
    single {
        EventConsumer(
            get<KafkaConfig>().brokers,
            get<KafkaConfig>().groupId,
            listOf(
                KafkaTopics.USER_CREATED,
                KafkaTopics.USER_UPDATED,
                KafkaTopics.USER_DEACTIVATED
            )
        )
    }
    single { UserEventListener() }
}