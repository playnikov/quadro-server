package com.quadro.plugins

import com.quadro.presentation.auth.authRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        authRoutes()

        get("/health") {
            call.respond(
                mapOf(
                    "status" to "OK",
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }

    }
}