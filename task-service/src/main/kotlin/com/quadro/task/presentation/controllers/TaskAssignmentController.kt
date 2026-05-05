package com.quadro.task.presentation.controllers

import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.task.domain.services.TaskAssignmentService
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
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun assignToTeam(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")

        val result = taskAssignmentService.assignTaskToTeam(taskId, teamId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun unassign(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")

        val result = taskAssignmentService.unassignTask(taskId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun validateUserAssignment(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val userId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("User ID is invalid")

        val result = taskAssignmentService.validateUserAssignment(taskId, userId)
        call.respond(HttpStatusCode.OK, mapOf("valid" to result))
    }

    suspend fun validateTeamAssignment(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")

        val result = taskAssignmentService.validateTeamAssignment(taskId, teamId)
        call.respond(HttpStatusCode.OK, mapOf("valid" to result))
    }
}