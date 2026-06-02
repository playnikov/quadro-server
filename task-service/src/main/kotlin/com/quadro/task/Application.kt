package com.quadro.task

import com.quadro.shared.di.sharedModule
import com.quadro.shared.plugins.configureMonitoring
import com.quadro.shared.plugins.configureSerialization
import com.quadro.shared.plugins.configureStatusPages
import com.quadro.shared.security.JwtValidator
import com.quadro.shared.security.configureSecurity
import com.quadro.task.di.kafkaModule
import com.quadro.task.di.taskModule
import com.quadro.task.plugins.configureDatabase
import com.quadro.task.plugins.configureKafka
import com.quadro.task.plugins.configureRouting
import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Application")
    try {
        logger.info("Starting Task Service...")
        io.ktor.server.netty.EngineMain.main(args)
    } catch (e: Exception) {
        logger.error("Failed to start Task Service", e)
        exitProcess(1)
    }
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(
            sharedModule(this@module, "task-service"),
            taskModule,
            kafkaModule
        )
    }
    configureSecurity(getKoin().get<JwtValidator>())
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureDatabase()
    configureKafka()
    configureRouting()
}