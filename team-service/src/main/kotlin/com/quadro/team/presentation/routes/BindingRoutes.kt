package com.quadro.team.presentation.routes

import com.quadro.team.presentation.controllers.ProjectBindingController
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class BindingRoutes(
    private val controller: ProjectBindingController
) {
    fun init(routing: Routing) {
        routing.route("/api/companies/{id}/teams/{teamId}") {
            post("/bind") { controller.bind(call) }
            post("/unbind") { controller.unbind(call) }
            get("/bind") { controller.getBindingByTeam(call) }
        }
    }
}