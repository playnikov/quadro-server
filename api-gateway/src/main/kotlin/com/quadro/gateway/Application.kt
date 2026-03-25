package com.quadro.gateway

import com.quadro.gateway.config.AppConfig
import com.quadro.gateway.di.gatewayModule
import com.quadro.gateway.plugins.configureRouting
import com.quadro.gateway.plugins.configureSerialization
import com.quadro.shared.plugins.configureMonitoring
import com.quadro.shared.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>)  {
    val logger = LoggerFactory.getLogger("Application")
    try {
        logger.info("Starting API Gateway...")
        embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            module()
        }.start(wait = true)

    } catch (e: Exception) {
        logger.error("Failed to start API Gateway", e)
        exitProcess(1)
    }
}

fun Application.module() {
    val appConfig = AppConfig.fromEnvironment()
    install(Koin) {
        slf4jLogger()
        modules(gatewayModule(appConfig))
    }
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureRouting()
}