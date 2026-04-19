package com.quadro.project.di

import com.quadro.project.infrastructure.messaging.listener.CompanyEventListener
import com.quadro.project.infrastructure.messaging.listener.CompanyMemberEventListener
import com.quadro.project.infrastructure.messaging.listener.UserEventListener
import com.quadro.project.infrastructure.messaging.processor.CompanyEventProcessor
import com.quadro.project.infrastructure.messaging.processor.CompanyMemberEventProcessor
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

    single(named("companyConsumer")) {
        EventConsumer(
            get<KafkaConfig>().bootstrapServers,
            get<KafkaConfig>().groupId,
            listOf(
                KafkaTopics.COMPANY_CREATED,
                KafkaTopics.COMPANY_UPDATED,
                KafkaTopics.COMPANY_DELETED
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
    single { UserEventListener() }
    single { CompanyEventListener() }
    single { CompanyMemberEventListener() }

    single { UserEventProcessor(get()) }
    single { CompanyEventProcessor(get()) }
    single { CompanyMemberEventProcessor(get()) }
}