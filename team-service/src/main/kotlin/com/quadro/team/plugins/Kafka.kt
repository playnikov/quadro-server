package com.quadro.team.plugins

import com.quadro.shared.data.messaging.EventProducer
import com.quadro.team.infrastructure.messaging.listener.CompanyEventListener
import com.quadro.team.infrastructure.messaging.listener.CompanyMemberEventListener
import com.quadro.team.infrastructure.messaging.listener.UserEventListener
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.log
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.ktor.ext.getKoin

fun Application.configureKafka() {
    val userListener: UserEventListener = getKoin().get()
    val companyListener: CompanyEventListener = getKoin().get()
    val companyMemberListener: CompanyMemberEventListener = getKoin().get()
    val producer: EventProducer = getKoin().get()

    userListener.start()
    log.info("User event listener started")

    companyListener.start()
    log.info("Company event listener started")

    companyMemberListener.start()
    log.info("Member event listener started")

    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            userListener.stop()
            companyListener.stop()
            companyMemberListener.stop()
            producer.close()
        }
        log.info("Kafka producer and listener closed")
    })

    monitor.subscribe(ApplicationStopping) {
        launch {
            producer.close()
            userListener.stop()
            companyListener.stop()
            companyMemberListener.stop()
            log.info("Kafka producer and listener stopped gracefully")
        }
    }
}

