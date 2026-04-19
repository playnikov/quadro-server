package com.quadro.project.plugins

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.time.Clock

import com.quadro.project.presentation.routes.ProjectRoutes
import io.ktor.server.auth.authenticate
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val projectRoutes by inject<ProjectRoutes>()

    routing {
        get("/") {
            call.respond(mapOf(
                "service" to "Project Service",
                "version" to "1.0.0",
                "timestamp" to Clock.System.now()
            ))
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "service" to "project-service",
                "timestamp" to Clock.System.now()
            ))
        }

        authenticate("auth-jwt") {
            projectRoutes.init(this)
        }
    }
}