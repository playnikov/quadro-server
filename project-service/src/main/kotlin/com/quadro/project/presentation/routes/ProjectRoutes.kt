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
        routing.route("/api/companies/{id}/projects") {
            authenticate("auth-jwt") {
                post { controller.createProject(call) }
                patch("/{projectId}") { controller.updateProject(call) }
                delete("/{projectId}") { controller.deleteProject(call) }

                get { controller.findByCompany(call) }
                get("/{projectId}") { controller.findById(call) }
                get("/{name}") { controller.findByName(call) }

                get("/my") { controller.findByUser(call) }

                patch("/{projectId}/{status}") { controller.updateStatus(call) }
            }
        }
    }
}