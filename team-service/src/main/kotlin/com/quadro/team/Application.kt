package com.quadro.team

import com.quadro.shared.plugins.configureMonitoring
import com.quadro.shared.plugins.configureStatusPages
import com.quadro.team.config.AppConfig
import com.quadro.team.di.companyModules
import com.quadro.team.plugins.configureDatabase
import com.quadro.team.plugins.configureRouting
import com.quadro.team.plugins.configureSecurity
import com.quadro.team.plugins.configureSerialization
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Application")
    try {
        logger.info("Starting Auth Service...")
        io.ktor.server.netty.EngineMain.main(args)
    } catch (e: Exception) {
        logger.error("Failed to start Auth Service", e)
        exitProcess(1)
    }
}

fun Application.module() {
    val appConfig = AppConfig.fromEnvironment()
    install(Koin) {
        slf4jLogger()
        modules(companyModules(appConfig))
    }
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureDatabase()
    configureSecurity()
    configureRouting()
}