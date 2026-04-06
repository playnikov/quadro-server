package com.quadro.company

import com.quadro.company.config.AppConfig
import com.quadro.company.di.companyModules
import com.quadro.company.di.kafkaModule
import com.quadro.company.plugins.configureDatabase
import com.quadro.company.plugins.configureKafka
import com.quadro.company.plugins.configureRouting
import com.quadro.company.plugins.configureSerialization
import com.quadro.shared.plugins.configureMonitoring
import com.quadro.shared.plugins.configureStatusPages
import com.quadro.shared.security.JwtValidator
import com.quadro.shared.security.configureSecurity
import io.ktor.server.application.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Application")
    try {
        logger.info("Starting Company Service...")
        io.ktor.server.netty.EngineMain.main(args)
    } catch (e: Exception) {
        logger.error("Failed to start Company Service", e)
        exitProcess(1)
    }
}

fun Application.module() {
    val appConfig = AppConfig.fromEnvironment()
    install(Koin) {
        slf4jLogger()
        modules(
            companyModules(appConfig),
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