package com.quadro.task.presentation.routes

import com.quadro.task.presentation.controllers.TaskCommentController
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

class TaskCommentRoutes(
    private val controller: TaskCommentController
) {
    fun init(routing: Route) {
        routing.route("/api/tasks/comments") {
            post { controller.createComment(call) }
            patch { controller.updateComment(call) }
            delete { controller.deleteComment(call) }
            get { controller.findById(call) }

            get("/task") { controller.findByTask(call) }
            get("/task/count") { controller.countByTask(call) }

            get("/replies") { controller.findReplies(call) }
        }
    }
}