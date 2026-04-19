package com.quadro.team.plugins

import com.quadro.team.presentation.routes.BindingRoutes
import com.quadro.team.presentation.routes.TeamMemberRoutes
import com.quadro.team.presentation.routes.TeamRoutes
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlin.time.Clock

fun Application.configureRouting() {
    val teamRoutes by inject<TeamRoutes>()
    val teamMemberRoutes by inject<TeamMemberRoutes>()
    val bindingRoutes by inject<BindingRoutes>()

    routing {
        get("/") {
            call.respond(mapOf(
                "service" to "Auth Service",
                "version" to "1.0.0",
                "timestamp" to Clock.System.now()
            ))
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "service" to "auth-service",
                "timestamp" to Clock.System.now()
            ))
        }

        authenticate("auth-jwt") {
            teamRoutes.init(this)
            teamMemberRoutes.init(this)
            bindingRoutes.init(this)
        }
    }
}