package com.quadro.task.presentation.controllers

import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.task.domain.services.TaskAssignmentService
import com.quadro.task.presentation.models.TaskResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class TaskAssignmentController(
    private val taskAssignmentService: TaskAssignmentService
) {
    suspend fun assignToUser(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val userId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("User ID is invalid")

        val result = taskAssignmentService.assignTaskToUser(taskId, userId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(TaskResponse.from(result)))
    }

    suspend fun unassign(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")

        val result = taskAssignmentService.unassignTask(taskId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(TaskResponse.from(result)))
    }
}