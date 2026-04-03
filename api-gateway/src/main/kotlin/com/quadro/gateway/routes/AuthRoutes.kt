package com.quadro.gateway.routes

import com.quadro.gateway.clients.AuthServiceClient
import com.quadro.gateway.plugins.principalUserId
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class AuthRoutes(
    private val authClient: AuthServiceClient
) {
    fun publicRoutes(routing: Route) {
        routing.route("/api/auth") {
            post("/register") {
                val body = call.receiveText()
                val response = authClient.register(body)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            post("/login") {
                val body = call.receiveText()
                val response = authClient.login(body)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            post("/refresh") {
                val body = call.receiveText()
                val response = authClient.refreshToken(body)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }


        }
    }

    fun protectedRoutes(routing: Route) {
        routing.route("/api/auth") {
            get("/me") {
                val userId = call.principalUserId() ?: return@get call.respond(HttpStatusCode.Forbidden)
                val response = authClient.getUser(userId)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            post("/logout") {
                val userId = call.principalUserId() ?: return@post call.respond(HttpStatusCode.Forbidden)
                val response = authClient.logout(userId)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            post("/change-password") {
                val userId = call.principalUserId()?: return@post call.respond(HttpStatusCode.Forbidden)
                val body = call.receiveText()
                val response = authClient.changePassword(userId, body)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }
        }
    }
}