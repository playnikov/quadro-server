package com.quadro.plugins

import com.quadro.presentation.auth.authRoutes
import com.quadro.presentation.company.companyRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        authRoutes()
        companyRoutes()

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