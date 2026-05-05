package com.quadro.task.plugins

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.task.infrastructure.messaging.listener.ProjectEventListener
import com.quadro.task.infrastructure.messaging.listener.TeamEventListener
import com.quadro.task.infrastructure.messaging.listener.UserEventListener
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.getKoin

fun Application.configureKafka() {
    val userListener: UserEventListener = getKoin().get()
    val projectListener: ProjectEventListener = getKoin().get()
    val teamListener: TeamEventListener = getKoin().get()
    val producer: EventProducer = getKoin().get()

    userListener.start()
    log.info("User event listener started")

    projectListener.start()
    log.info("Project event listener started")

    teamListener.start()
    log.info("Team event listener started")

    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            userListener.stop()
            projectListener.stop()
            teamListener.stop()
            producer.close()
        }
        log.info("Kafka producer and listener closed")
    })

    monitor.subscribe(ApplicationStopping) {
        launch {
            producer.close()
            userListener.stop()
            teamListener.stop()
            projectListener.stop()
            log.info("Kafka producer and listener stopped gracefully")
        }
    }
}