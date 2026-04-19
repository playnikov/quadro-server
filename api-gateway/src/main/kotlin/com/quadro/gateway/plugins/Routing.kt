package com.quadro.gateway.plugins

import com.quadro.gateway.routes.AuthRoutes
import com.quadro.gateway.routes.CompanyRoutes
import com.quadro.gateway.routes.InvitationRoutes
import com.quadro.gateway.routes.ProjectRoutes
import com.quadro.gateway.routes.TeamRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.headers
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveChannel
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.method
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Application.configureRouting() {
    val authRoutes by inject<AuthRoutes>()
    val companyRoutes by inject<CompanyRoutes>()
    val invitationRoutes by inject<InvitationRoutes>()
    val projectRoutes by inject<ProjectRoutes>()
    val teamRoutes by inject<TeamRoutes>()

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

        authenticate("auth-jwt") {
            authRoutes.protectedRoutes(this)
            companyRoutes.protectedRoutes(this)
            invitationRoutes.protectedRoutes(this)
            projectRoutes.protectedRoutes(this)
            teamRoutes.protectedRoutes(this)
        }
    }
}

fun Route.proxyTo(client: HttpClient, targetBaseUrl: String) {
    handle {
        val targetUrl = "$targetBaseUrl${call.request.uri}"
        val response: HttpResponse = client.request(targetUrl) {
            method = call.request.httpMethod
            headers.appendAll(call.request.headers)
            if (call.request.httpMethod in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                setBody(call.receiveChannel())
            }
        }
        call.respondText(
            text = response.bodyAsText(),
            contentType = ContentType.Application.Json,
            status = response.status
        )
    }
}