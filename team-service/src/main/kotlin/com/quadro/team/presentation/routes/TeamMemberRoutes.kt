package com.quadro.team.presentation.routes

import com.quadro.team.presentation.controllers.TeamMemberController
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class TeamMemberRoutes(
    private val controller: TeamMemberController
) {
    fun init(routing: Route) {
        routing.route("/api/teams/members") {
            get { controller.getMembers(call) }
            post { controller.addMember(call) }
            delete { controller.removeMember(call) }
            patch { controller.changeRole(call) }
        }
    }
}