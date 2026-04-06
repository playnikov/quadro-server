package com.quadro.gateway.routes

import com.quadro.gateway.config.ServiceUrls
import com.quadro.gateway.plugins.proxyTo
import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class CompanyRoutes(
    private val client: HttpClient,
    serviceBaseUrl: ServiceUrls
) {
    private val companyServiceBaseUrl = serviceBaseUrl.company
    fun protectedRoutes(routing: Route) {
        routing.route("/api/companies") {
            proxyTo(client, companyServiceBaseUrl)

            route("/my") {
                proxyTo(client, companyServiceBaseUrl)
            }

            route("/{id}") {
                proxyTo(client, companyServiceBaseUrl)
            }

            route("/{id}/leave") {
                proxyTo(client, companyServiceBaseUrl)
            }

            route("/{id}/members") {
                proxyTo(client, companyServiceBaseUrl)
            }

            route("/{id}/members/{userId}/role") {
                proxyTo(client, companyServiceBaseUrl)
            }

            route("/{id}/members/{userId}") {
                proxyTo(client, companyServiceBaseUrl)
            }
        }
    }
}