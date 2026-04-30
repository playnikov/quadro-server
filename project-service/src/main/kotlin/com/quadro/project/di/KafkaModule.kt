package com.quadro.project.di

import com.quadro.project.infrastructure.messaging.listener.UserEventListener
import com.quadro.project.infrastructure.messaging.processor.UserEventProcessor
import com.quadro.shared.data.config.KafkaConfig
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import org.koin.core.qualifier.named
import org.koin.core.scope.get
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

    single { UserEventListener() }

    single { UserEventProcessor(get()) }
}