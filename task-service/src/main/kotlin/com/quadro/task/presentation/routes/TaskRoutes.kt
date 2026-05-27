package com.quadro.task.presentation.routes

import com.quadro.task.presentation.controllers.TaskController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class TaskRoutes(
    private val controller: TaskController
) {
    fun init(routing: Route) {
        routing.route("/api/tasks") {
            post { controller.createTask(call) }
            patch { controller.updateTask(call) }
            delete { controller.deleteTask(call) }
            get { controller.findById(call) }

            get("/project") { controller.findByProject(call) }
            get("/sprint") { controller.findBySprint(call) }
            get("/assignee") { controller.findByAssignee(call) }
            get("/parent") { controller.findByParent(call) }

            get("/history") { controller.getHistory(call) }

            get("/deadlines") { controller.getUpcomingDeadlines(call) }
        }
    }
}