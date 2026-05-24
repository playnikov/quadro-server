package com.quadro.notification.plugins

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.notification.infrastructure.messaging.listener.ProjectEventListener
import com.quadro.notification.infrastructure.messaging.listener.TaskEventListener
import com.quadro.notification.infrastructure.messaging.listener.TeamEventListener
import com.quadro.notification.infrastructure.messaging.listener.UserEventListener
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.getKoin

fun Application.configureKafka() {
    val userListener: UserEventListener = getKoin().get()
    val projectListener: ProjectEventListener = getKoin().get()
    val taskListener: TaskEventListener = getKoin().get()
    val producer: EventProducer = getKoin().get()

    taskListener.start()
    log.info("Task event listener started")

    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            taskListener.stop()
            producer.close()
        }
        log.info("Kafka producer and listener closed")
    })

    monitor.subscribe(ApplicationStopping) {
        launch {
            taskListener.stop()
            producer.close()
            log.info("Kafka producer and listener stopped gracefully")
        }
    }
}