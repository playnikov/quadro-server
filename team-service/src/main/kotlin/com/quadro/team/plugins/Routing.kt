package com.quadro.team.plugins

import com.quadro.company.presentation.routes.CompanyRoutes
import com.quadro.company.presentation.routes.InvitationRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlin.time.Clock

fun Application.configureRouting() {
    val companyRoutes by inject<CompanyRoutes>()
    val invitationRoutes by inject<InvitationRoutes>()

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

        companyRoutes.init(this)
        invitationRoutes.init(this)
    }
}