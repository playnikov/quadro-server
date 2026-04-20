package com.quadro.task.di

import com.quadro.shared.data.config.KafkaConfig
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.task.infrastructure.messaging.ProjectEventListener
import com.quadro.task.infrastructure.messaging.TeamEventListener
import com.quadro.task.infrastructure.messaging.UserEventListener
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

    single(named("memberConsumer")) {
        EventConsumer(
            get<KafkaConfig>().bootstrapServers,
            get<KafkaConfig>().groupId,
            listOf(
                KafkaTopics.COMPANY_MEMBER_ADDED,
                KafkaTopics.COMPANY_MEMBER_ROLE_UPDATED,
                KafkaTopics.COMPANY_MEMBER_REMOVED
            )
        )
    }

    single(named("projectConsumer")) {
        EventConsumer(
            get<KafkaConfig>().bootstrapServers,
            get<KafkaConfig>().groupId,
            listOf(
                KafkaTopics.PROJECT_CREATED,
                KafkaTopics.PROJECT_UPDATED,
                KafkaTopics.PROJECT_DELETED
            )
        )
    }

    single { UserEventListener() }
    single { TeamEventListener() }
    single { ProjectEventListener() }
}