package com.quadro.notification

import com.quadro.shared.di.sharedModule
import com.quadro.shared.plugins.configureMonitoring
import com.quadro.shared.plugins.configureStatusPages
import com.quadro.shared.security.JwtValidator
import com.quadro.shared.security.configureSecurity
import com.quadro.notification.di.kafkaModule
import com.quadro.notification.di.notificationModule
import com.quadro.notification.infrastructure.messaging.listener.TaskEventListener
import com.quadro.notification.plugins.configureDatabase
import com.quadro.notification.plugins.configureKafka
import com.quadro.notification.plugins.configureRouting
import com.quadro.notification.plugins.configureSerialization
import com.quadro.notification.plugins.configureWebSocket
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Application")
    try {
        logger.info("Starting Notification Service...")
        io.ktor.server.netty.EngineMain.main(args)
    } catch (e: Exception) {
        logger.error("Failed to start Notification Service", e)
        exitProcess(1)
    }
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(
            sharedModule(this@module, "notification-service"),
            notificationModule,
            kafkaModule
        )
    }
    configureSecurity(getKoin().get<JwtValidator>())
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
//    configureDatabase()
    configureKafka()
    configureWebSocket()
    configureRouting()
}