package com.quadro.company.plugins

import com.quadro.shared.kafka.KafkaConsumerService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.koin.ktor.ext.inject

fun Application.configureKafka() {
    val producer: KafkaProducer<String, String> by inject()
    val consumerService: KafkaConsumerService by inject()

    consumerService.start()
    log.info("Kafka consumer started")

    monitor.subscribe(ApplicationStopping) {
        producer.close()
        log.info("Kafka producer closed")
        consumerService.stop()
        log.info("Kafka consumer stopped")
    }
}

