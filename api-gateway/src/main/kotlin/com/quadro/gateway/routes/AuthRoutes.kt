package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import com.quadro.shared.security.getUserId
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class AuthRoutes(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val authServiceBaseUrl = serviceBaseUrl.auth
    fun publicRoutes(routing: Route) {
        routing.route("/api/auth") {
            route("/register") {
                proxyTo(client, authServiceBaseUrl)
            }

            route("/login") {
                proxyTo(client, authServiceBaseUrl)
            }

            route("/refresh") {
                proxyTo(client, authServiceBaseUrl)
            }
        }
    }

    fun protectedRoutes(routing: Route) {

        routing.route("/api/users") {
            route("/profile") {
                proxyTo(client, authServiceBaseUrl)
            }

            route("/logout") {
                proxyTo(client, authServiceBaseUrl)
            }

            route("/change-password") {
                proxyTo(client, authServiceBaseUrl)
            }
        }
    }
}