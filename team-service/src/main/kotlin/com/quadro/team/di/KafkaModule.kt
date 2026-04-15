package com.quadro.team.di

import com.quadro.shared.data.config.KafkaConfig
import com.quadro.shared.data.messaging.EventConsumer
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.team.infrastructure.messaging.listener.CompanyEventListener
import com.quadro.team.infrastructure.messaging.listener.CompanyMemberEventListener
import com.quadro.team.infrastructure.messaging.listener.ProjectEventListener
import com.quadro.team.infrastructure.messaging.listener.UserEventListener
import com.quadro.team.infrastructure.messaging.processor.CompanyEventProcessor
import com.quadro.team.infrastructure.messaging.processor.CompanyMemberEventProcessor
import com.quadro.team.infrastructure.messaging.processor.ProjectEventProcessor
import com.quadro.team.infrastructure.messaging.processor.UserEventProcessor
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
    single { CompanyEventListener() }
    single { CompanyMemberEventListener() }
    single { ProjectEventListener() }

    single { UserEventProcessor(get()) }
    single { CompanyEventProcessor(get()) }
    single { CompanyMemberEventProcessor(get()) }
    single { ProjectEventProcessor(get()) }
}