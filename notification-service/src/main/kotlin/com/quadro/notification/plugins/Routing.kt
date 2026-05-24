package com.quadro.notification.plugins

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respond(mapOf(
                "service" to "Notification Service",
                "version" to "1.0.0",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "service" to "notification-service",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }
    }
}