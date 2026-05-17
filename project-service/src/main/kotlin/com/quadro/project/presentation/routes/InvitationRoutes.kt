package com.quadro.project.presentation.routes

import com.quadro.project.presentation.controllers.InvitationController
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
        routing.route("/api/projects") {
            authenticate("auth-jwt") {
                route("/invitations") {
                    post { invitationController.createInvitation(call) }
                    get { invitationController.getInvitations(call) }
                    delete { invitationController.cancelInvitation(call) }
                    get("/email") { invitationController.getInvitationsByEmail(call) }
                }

                route("/invite") {
                    post { invitationController.acceptInvitation(call) }
                }
            }
        }
    }
}