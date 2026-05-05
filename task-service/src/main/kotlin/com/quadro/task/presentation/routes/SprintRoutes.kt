package com.quadro.task.presentation.routes

import com.quadro.task.presentation.controllers.SprintController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class SprintRoutes(
    private val controller: SprintController
) {
    fun init(routing: Route) {
        routing.route("/api/sprints") {
            post { controller.createSprint(call) }
            patch { controller.updateSprint(call) }
            delete { controller.deleteSprint(call) }

            get{ controller.findById(call) }
            get("/project") { controller.findByProject(call) }
        }
    }
}