package com.quadro.gateway.plugins

import com.quadro.gateway.routes.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    install(CORS) {
        allowHost("localhost:8080", schemes = listOf("http"))
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
    }

    val authRoutes by inject<AuthRoutes>()
    val invitationRoutes by inject<InvitationRoutes>()
    val projectRoutes by inject<ProjectRoutes>()
    val taskRoutes by inject<TaskRoutes>()
    val teamRoutes by inject<TeamRoutes>()

    routing {
        get("/") {
            call.respond(
                mapOf(
                    "service" to "Api Gateway",
                    "version" to "1.0.0",
                    "timestamp" to System.currentTimeMillis().toString()
                )
            )
        }

        get("/health") {
            call.respond(
                mapOf(
                    "status" to "UP",
                    "service" to "api-gateway",
                    "timestamp" to System.currentTimeMillis().toString()
                )
            )
        }

        authRoutes.publicRoutes(this)

        authenticate("auth-jwt") {
            authRoutes.protectedRoutes(this)
            invitationRoutes.protectedRoutes(this)
            projectRoutes.protectedRoutes(this)
            teamRoutes.protectedRoutes(this)
            taskRoutes.protectedRoutes(this)
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