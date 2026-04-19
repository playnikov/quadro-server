package com.quadro.team.presentation.routes

import com.quadro.team.presentation.controllers.TeamController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class TeamRoutes(
    private val controller: TeamController,
) {
    fun init(routing: Route) {
        routing.route("/api/companies/{id}/teams") {
            authenticate("auth-jwt") {
                post { controller.createTeam(call) }
                get("/{teamId}") { controller.getById(call) }
                get { controller.getByCompany(call) }
                patch("/{teamId}") { controller.update(call) }
                delete("/{teamId}") { controller.delete(call) }
            }
        }
    }
}