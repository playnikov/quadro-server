package com.quadro.company.presentation.routes

import com.quadro.company.presentation.controllers.InvitationController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class InvitationRoutes(
    private val invitationController: InvitationController
) {
    fun init(routing: Route) {
        routing.route("/api/companies") {
            authenticate("auth-jwt") {
                route("/{id}/invitations/") {
                    post { invitationController.createInvitation(call) }
                    get { invitationController.getInvitations(call) }
                    delete("/{invitationId}") { invitationController.cancelInvitation(call) }
                }

                route("/invite/{token}") {
                    post { invitationController.acceptInvitation(call) }
                }
            }
        }
    }
}