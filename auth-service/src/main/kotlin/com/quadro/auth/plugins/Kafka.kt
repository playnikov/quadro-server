package com.quadro.auth.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.koin.ktor.ext.inject

fun Application.configureKafka() {
    val producer: KafkaProducer<String, String> by inject()
    monitor.subscribe(ApplicationStopping) {
        producer.close()
        log.info("Kafka producer closed")
    }
}

