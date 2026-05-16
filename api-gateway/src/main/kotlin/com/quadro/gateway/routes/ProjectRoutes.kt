package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import io.ktor.client.HttpClient
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

class ProjectRoutes(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val projectServiceBaseUrl = serviceBaseUrl.project
    fun protectedRoutes(routing: Route) {
        routing.route("/api/projects") {
            proxyTo(client,projectServiceBaseUrl )
            route("/status") { proxyTo(client,projectServiceBaseUrl ) }
            route("/my") { proxyTo(client,projectServiceBaseUrl ) }
            route("/search") { proxyTo(client,projectServiceBaseUrl ) }

            route("/members") {
                proxyTo(client,projectServiceBaseUrl )
                route("/leave") { proxyTo(client,projectServiceBaseUrl ) }
            }
        }
    }
}