package com.quadro.auth

import com.quadro.auth.di.authModule
import com.quadro.auth.di.kafkaModule
import com.quadro.auth.di.userModule
import com.quadro.auth.plugins.configureDatabase
import com.quadro.auth.plugins.configureKafka
import com.quadro.shared.plugins.configureMonitoring
import com.quadro.auth.plugins.configureRouting
import com.quadro.auth.plugins.configureSerialization
import com.quadro.auth.plugins.seedSuperAdminOnStart
import com.quadro.shared.di.sharedModule
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
        logger.info("Starting Auth Service...")
        io.ktor.server.netty.EngineMain.main(args)
    } catch (e: Exception) {
        logger.error("Failed to start Auth Service", e)
        exitProcess(1)
    }
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(
            sharedModule(this@module, "auth-service"),
            authModule,
            userModule,
            kafkaModule
        )
    }
    configureSecurity(getKoin().get<JwtValidator>())
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureKafka()
    configureDatabase()
    seedSuperAdminOnStart()
    
    configureRouting()
}