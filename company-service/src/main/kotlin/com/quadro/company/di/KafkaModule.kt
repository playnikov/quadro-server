package com.quadro.company.di

import com.quadro.company.infrastructure.messaging.ProjectEventListener
import com.quadro.company.infrastructure.messaging.ProjectEventProcessor
import com.quadro.company.infrastructure.messaging.UserEventListener
import com.quadro.company.infrastructure.messaging.UserEventProcessor
import com.quadro.shared.data.config.KafkaConfig
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import org.koin.core.qualifier.named
import org.koin.dsl.module

val kafkaModule = module {
    single { EventProducer(get()) }
    single(named("userConsumer")) {
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

    single(named("projectConsumer")) {
        EventConsumer(
            get<KafkaConfig>().bootstrapServers,
            get<KafkaConfig>().groupId,
            listOf(
                KafkaTopics.PROJECT_CREATED,
                KafkaTopics.PROJECT_DELETED,
                KafkaTopics.PROJECT_ARCHIVED
            )
        )
    }

    single { UserEventListener() }
    single { UserEventProcessor(get()) }

    single { ProjectEventListener() }
    single { ProjectEventProcessor(get()) }
}