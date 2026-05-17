package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import io.ktor.client.HttpClient
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

class InvitationRoutes(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val projectServiceBaseUrl = serviceBaseUrl.project
    fun protectedRoutes(routing: Route) {
        routing.route("/api/projects") {
            route("/invitations") {
                proxyTo(client, projectServiceBaseUrl)

                route("/email") { proxyTo(client, projectServiceBaseUrl) }
            }

            route("/invite") {
                proxyTo(client, projectServiceBaseUrl)
            }
        }
    }
}