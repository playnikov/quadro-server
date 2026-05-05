package com.quadro.task.presentation.routes

import com.quadro.task.presentation.controllers.TaskStatusController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route

class TaskStatusRoutes(
    private val controller: TaskStatusController
) {
    fun init(routing: Route) {
        routing.route("/api/tasks") {
            patch("/status/{status}") { controller.transitionStatus(call) }
            get("/validate/status/{status}") { controller.validateTransition(call) }
            patch("/start") { controller.start(call) }
            patch("/complete") { controller.complete(call) }
            patch("/cancel") { controller.cancel(call) }
            patch("/reopen") { controller.reopen(call) }
        }
    }
}