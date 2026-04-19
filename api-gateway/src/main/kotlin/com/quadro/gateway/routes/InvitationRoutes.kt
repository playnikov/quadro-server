package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.text.removePrefix

class InvitationRoutes(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val companyServiceBaseUrl = serviceBaseUrl.company
    fun protectedRoutes(routing: Route) {
        routing.route("/api/companies") {
            route("/{companyId}/invitations/") {
                proxyTo(client, companyServiceBaseUrl)
            }

            route("/invite") {
                proxyTo(client, companyServiceBaseUrl)
            }
        }
    }
}