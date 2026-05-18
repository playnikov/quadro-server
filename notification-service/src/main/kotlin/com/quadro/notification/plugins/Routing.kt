package com.quadro.notification.plugins

import com.quadro.notification.domain.services.NotificationService
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val notificationService: NotificationService by inject()

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

        authenticate("auth-jwt") {
            webSocket("/ws/notifications") {
                try {
                    notificationService.addSession(this)
                    // Keep the connection alive
                    incoming.collect()
                } finally {
                    notificationService.removeSession(this.hashCode().toString())
                }
            }
        }
    }
}