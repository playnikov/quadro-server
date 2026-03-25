package com.quadro.gateway.plugins

import com.quadro.gateway.routes.AuthRoutes
import com.quadro.gateway.routes.CompanyRoutes
import com.quadro.gateway.routes.InvitationRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Application.configureRouting() {
    val authRoutes by inject<AuthRoutes>()
    val companyRoutes by inject<CompanyRoutes>()
    val invitationRoutes by inject<InvitationRoutes>()

    routing {
        get("/") {
            call.respond(mapOf(
                "service" to "Api Gateway",
                "version" to "1.0.0",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "service" to "api-gateway",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }

        authRoutes.publicRoutes(this)
        companyRoutes.protectedRoutes(this)
        invitationRoutes.protectedRoutes(this)
    }
}