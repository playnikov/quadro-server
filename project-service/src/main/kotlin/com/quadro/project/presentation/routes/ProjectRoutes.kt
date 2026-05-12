package com.quadro.project.presentation.routes

import com.quadro.project.presentation.controllers.ProjectController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class ProjectRoutes(
    private val controller: ProjectController
) {
    fun init(routing: Route) {
        routing.route("/api/projects") {
            authenticate("auth-jwt") {
                post { controller.createProject(call) }
                patch { controller.updateProject(call) }
                delete { controller.deleteProject(call) }

                get { controller.findById(call) }
                get("/search") { controller.findByName(call) }

                get("/my") { controller.findByUser(call) }

                patch("/status") { controller.updateStatus(call) }
            }
        }
    }
}