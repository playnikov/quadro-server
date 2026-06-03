package com.quadro.gateway

import com.quadro.gateway.di.gatewayModule
import com.quadro.gateway.plugins.configureRouting
import com.quadro.gateway.plugins.configureWebSocket
import com.quadro.shared.di.sharedModule
import com.quadro.shared.plugins.configureMonitoring
import com.quadro.shared.plugins.configureSerialization
import com.quadro.shared.plugins.configureStatusPages
import com.quadro.shared.security.JwtValidator
import com.quadro.shared.security.configureSecurity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Application")
    try {
        logger.info("Starting API Gateway...")
        io.ktor.server.netty.EngineMain.main(args)

    } catch (e: Exception) {
        logger.error("Failed to start API Gateway", e)
        exitProcess(1)
    }
}

fun Application.module() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Cookie)
    }

    install(Koin) {
        slf4jLogger()
        modules(
            sharedModule(this@module, "api-gateway"),
            gatewayModule(this@module)
        )
    }
    configureSecurity(getKoin().get<JwtValidator>())
    configureSerialization()
    configureMonitoring()
    configureStatusPages()
    configureWebSocket()
    configureRouting()
}