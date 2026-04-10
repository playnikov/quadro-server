package com.quadro.company.plugins

import com.quadro.company.infrastructure.messaging.UserEventListener
import com.quadro.shared.kafka.EventProducer
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import org.apache.kafka.clients.producer.KafkaProducer
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject

fun Application.configureKafka() {
    val listener: UserEventListener = getKoin().get()
    val producer: EventProducer = getKoin().get()

    listener.start()
    Runtime.getRuntime().addShutdownHook(Thread {
        listener.stop()
    })

    listener.start()
    log.info("User event listener started")

    monitor.subscribe(ApplicationStopping) {
        producer.close()
        log.info("Kafka producer closed")
        listener.stop()
        log.info("User event listener stopped")
    }
}

