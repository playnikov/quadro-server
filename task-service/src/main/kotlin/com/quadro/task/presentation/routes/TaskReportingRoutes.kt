package com.quadro.task.presentation.routes

import com.quadro.task.presentation.controllers.TaskReportingController
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class TaskReportingRoutes(
    private val controller: TaskReportingController
) {
    fun init(routing: Route) {
        routing.route("/api/tasks/reporting") {
            get("/backlog") { controller.getBacklogCount(call) }
            get("/todo") { controller.getTodoCount(call) }
            get("/in-progress") { controller.getInProgressCount(call) }
            get("/in-review") { controller.getInReviewCount(call) }
            get("/done") { controller.getDoneCount(call) }
            get("/cancelled") { controller.getCancelledCount(call) }
            get("/counts") { controller.getTaskCounts(call) }
            get("/overdue") { controller.getOverdueTasks(call) }
            get("/avg-completion") { controller.getAverageCompletionDays(call) }
            get("/completion-rate") { controller.getCompletionRate(call) }
            get("/velocity") { controller.getVelocity(call) }
            get("/period") { controller.getReporting(call) }
        }
    }
}