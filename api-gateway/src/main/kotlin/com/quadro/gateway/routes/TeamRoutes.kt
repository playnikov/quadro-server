package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import io.ktor.client.HttpClient
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

class TeamRoutes(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val teamServiceBaseUrl = serviceBaseUrl.team

    fun protectedRoutes(routing: Route) {
        routing.route("/api/companies/{id}/teams") {
            proxyTo(client, teamServiceBaseUrl)
            route("/{teamId}") { proxyTo(client, teamServiceBaseUrl) }
        }
    }
}