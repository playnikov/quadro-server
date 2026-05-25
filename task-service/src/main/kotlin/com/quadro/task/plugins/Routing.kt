package com.quadro.task.plugins

import com.quadro.task.presentation.routes.TaskCommentRoutes
import com.quadro.task.presentation.routes.SprintRoutes
import com.quadro.task.presentation.routes.TaskAssignmentRoutes
import com.quadro.task.presentation.routes.TaskReportingRoutes
import com.quadro.task.presentation.routes.TaskRoutes
import com.quadro.task.presentation.routes.TaskStatusRoutes
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import kotlin.time.Clock

fun Application.configureRouting() {
    val taskRoutes by inject<TaskRoutes>()
    val taskStatusRoutes by inject<TaskStatusRoutes>()
    val taskReportingRoutes by inject<TaskReportingRoutes>()
    val taskCommentRoutes by inject<TaskCommentRoutes>()
    val taskAssignmentRoutes by inject<TaskAssignmentRoutes>()
    val sprintRoutes by inject<SprintRoutes>()

    routing {
        get("/") {
            call.respond(mapOf(
                "service" to "Task Service",
                "version" to "1.0.0",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }

        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "service" to "task-service",
                "timestamp" to System.currentTimeMillis().toString()
            ))
        }

        authenticate("auth-jwt") {
            taskRoutes.init(this)
            taskStatusRoutes.init(this)
            taskReportingRoutes.init(this)
            taskAssignmentRoutes.init(this)
            sprintRoutes.init(this)
            taskCommentRoutes.init(this)
        }
    }

}