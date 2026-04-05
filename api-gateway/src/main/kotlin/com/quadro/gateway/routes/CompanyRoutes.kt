package com.quadro.gateway.routes

import com.quadro.gateway.clients.AuthServiceClient
import com.quadro.gateway.clients.CompanyServiceClient
import com.quadro.gateway.plugins.principalUserId
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class CompanyRoutes(
    private val companyServiceClient: CompanyServiceClient
) {
    private fun getToken(call: ApplicationCall): String {
        return call.request.headers["Authorization"]?.removePrefix("Bearer ")
            ?: throw IllegalArgumentException("Missing or invalid token")
    }

    fun protectedRoutes(routing: Route) {
        routing.route("/api/companies") {
            post {
                val token = getToken(call)
                val response = companyServiceClient.createCompany(token, call.receiveText())
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            get("/my") {
                val token = getToken(call)
                val filter = call.request.queryParameters["filter"]
                val response = companyServiceClient.getUserCompanies(token, filter)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            get("/{id}") {
                val token = getToken(call)
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing company id")
                val response = companyServiceClient.getCompany(token, id)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            put("/{id}") {
                val token = getToken(call)
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing company id")
                val body = call.receiveText()
                val response = companyServiceClient.updateCompany(token, id, body)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            delete("/{id}") {
                val token = getToken(call)
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing company id")
                val response = companyServiceClient.deleteCompany(token, id)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            get("/{id}/members") {
                val token = getToken(call)
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing company id")
                val response = companyServiceClient.getCompanyMembers(token, id)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            patch("/{id}/members/{userId}/role") {
                val token = getToken(call)
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing company id")
                val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing user id")
                val body = call.receiveText()
                val response = companyServiceClient.updateMemberRole(token, id, userId, body)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            delete("/{id}/members/{userId}") {
                val token = getToken(call)
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing company id")
                val userId = call.parameters["userId"] ?: throw IllegalArgumentException("Missing user id")
                val response = companyServiceClient.removeMember(token, id, userId)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }

            post("/{id}/leave") {
                val token = getToken(call)
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing company id")
                val response = companyServiceClient.leaveCompany(token, id)
                call.respondText(
                    text = response.bodyAsText(),
                    contentType = ContentType.Application.Json,
                    status = response.status
                )
            }
        }
    }
}