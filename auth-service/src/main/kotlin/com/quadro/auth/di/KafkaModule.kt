package com.quadro.auth.di

import com.quadro.auth.config.KafkaConfig
import com.quadro.auth.domain.services.EventPublisher
import com.quadro.auth.domain.services.EventPublisherImpl
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
}

private fun createProducer(bootstrapServers: String): KafkaProducer<String, String> {
    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        put(ProducerConfig.CLIENT_ID_CONFIG, "auth-producer")
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.RETRIES_CONFIG, 3)
    }
    return KafkaProducer(props)
}