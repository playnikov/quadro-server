package com.quadro.gateway.plugins

import com.quadro.gateway.routes.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authRoutes by inject<AuthRoutes>()
    val invitationRoutes by inject<InvitationRoutes>()
    val projectRoutes by inject<ProjectRoutes>()
    val taskRoutes by inject<TaskRoutes>()
    val teamRoutes by inject<TeamRoutes>()
    val webSocket by inject<WebSocket>()

    routing {
        options("/{...}") {
            call.respond(HttpStatusCode.OK)
        }

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
        webSocket.init(this)

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
            if (call.request.httpMethod in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch, HttpMethod.Options)) {
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

suspend fun DefaultWebSocketSession.proxyTo(client: HttpClient, baseUrl: String, userId: String, path: String, queryParams: Map<String, String> = emptyMap()) {
    val wsBase = baseUrl
        .replace("http://", "ws://")
        .replace("https://", "wss://")
        .removeSuffix("/")

    val wsUrl = URLBuilder().apply {
        takeFrom(wsBase + path)
        queryParams.forEach { (key, value) ->
            parameters.append(key, value)
        }
    }.build().toString()

    val upstream = client.webSocketSession(wsUrl) {
        header("X-User-Id", userId)
    }
    coroutineScope {
        launch {
            try {
                for (frame in incoming) {
                    if (frame is Frame.Close) break
                    upstream.send(frame)
                }
                upstream.close()
            } catch (e: ClosedReceiveChannelException) {
                println(e.message)
            } catch (e: Exception) {
                println(e.message)
            }
        }

        launch {
            try {
                for (frame in upstream.incoming) {
                    if (frame is Frame.Close) break
                    send(frame)
                }
                close()
            } catch (e: ClosedReceiveChannelException) {
                println(e.message)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
}