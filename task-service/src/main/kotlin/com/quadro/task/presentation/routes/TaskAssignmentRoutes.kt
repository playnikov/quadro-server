package com.quadro.task.presentation.routes

import com.quadro.task.presentation.controllers.TaskAssignmentController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route

class TaskAssignmentRoutes(
    private val controller: TaskAssignmentController
) {
    fun init(routing: Route) {
        routing.route("/api/tasks") {
            patch("/assign/user") { controller.assignToUser(call) }
            patch("/unassign") { controller.unassign(call) }
        }
    }
}