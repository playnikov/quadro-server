package com.quadro.company.plugins

import com.quadro.company.infrastructure.messaging.UserEventListener
import com.quadro.shared.data.messaging.EventProducer
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.getKoin

fun Application.configureKafka() {
    val listener: UserEventListener = getKoin().get()
    val producer: EventProducer = getKoin().get()

    listener.start()
    log.info("User event listener started")

    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            listener.stop()
            producer.close()
        }
        log.info("Kafka producer and listener closed")
    })

    monitor.subscribe(ApplicationStopping) {
        launch {
            producer.close()
            listener.stop()
            log.info("Kafka producer and listener stopped gracefully")
        }
    }
}

