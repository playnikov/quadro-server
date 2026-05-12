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
    private val controller: TeamController
) {
    fun init(routing: Route) {
        routing.route("/api/teams") {
            post { controller.createTeam(call) }
            get { controller.getById(call) }
            patch { controller.update(call) }
            delete { controller.delete(call) }
        }
    }
}