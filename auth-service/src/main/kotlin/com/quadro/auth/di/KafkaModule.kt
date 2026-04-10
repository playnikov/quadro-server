package com.quadro.auth.di

import com.quadro.auth.config.KafkaConfig
import com.quadro.shared.kafka.EventConsumer
import com.quadro.shared.kafka.EventProducer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.dsl.module
import java.util.Properties

val kafkaModule = module {
    single { EventProducer(get<KafkaConfig>().brokers) }
}
