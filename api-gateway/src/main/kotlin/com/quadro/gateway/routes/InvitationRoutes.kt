package com.quadro.gateway.routes

import com.quadro.gateway.clients.InvitationServiceClient
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
    private val invitationServiceClient: InvitationServiceClient
) {
    private fun getToken(call: ApplicationCall): String {
        return call.request.headers["Authorization"]?.removePrefix("Bearer ")
            ?: throw IllegalArgumentException("Missing or invalid token")
    }

    fun protectedRoutes(routing: Route) {
        routing.route("/api/companies/{companyId}/invitations/") {
            post {
                val token = getToken(call)
                val companyId = call.parameters["companyId"] ?: throw IllegalArgumentException("Missing company id")
                val response = invitationServiceClient.createInvitation(token, companyId, call.receiveText())
                call.respond(response.status, response.bodyAsText())
            }

            get {
                val token = getToken(call)
                val companyId = call.parameters["companyId"] ?: throw IllegalArgumentException("Missing company id")
                val response = invitationServiceClient.getInvitations(token, companyId)
                call.respond(response.status, response.bodyAsText())
            }

            delete {
                val token = getToken(call)
                val companyId = call.parameters["companyId"] ?: throw IllegalArgumentException("Missing company id")
                val response = invitationServiceClient.cancelInvitation(token, companyId)
                call.respond(response.status, response.bodyAsText())
            }
        }

        routing.route("/invite/{token}") {
            post {
                val token = getToken(call)
                val tokenAccept = call.parameters["token"] ?: throw IllegalArgumentException("Missing company id")
                val response = invitationServiceClient.acceptInvitation(token, tokenAccept)
                call.respond(response.status, response.bodyAsText())
            }
        }
    }
}