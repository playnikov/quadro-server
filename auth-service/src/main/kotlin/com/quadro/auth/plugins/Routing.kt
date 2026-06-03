package com.quadro.auth.plugins

import com.quadro.auth.presentation.routes.AuthRoutes
import com.quadro.auth.presentation.routes.UserRoutes
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authRoutes by inject<AuthRoutes>()
    val userRoutes by inject<UserRoutes>()

    routing {
        get("/") {
            call.respond(mapOf(
                "service" to "Auth Service",
                "version" to "1.0.0",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "service" to "auth-service",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }

        authRoutes.publicRoute(this)

        authenticate("auth-jwt") {
            userRoutes.protectedRoute(this)
            authRoutes.protectedRoute(this)
        }
    }
}