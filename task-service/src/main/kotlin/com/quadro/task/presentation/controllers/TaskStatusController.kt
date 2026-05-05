package com.quadro.task.presentation.controllers

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.services.TaskStatusService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class TaskStatusController(
    private val taskStatusService: TaskStatusService
) {
    suspend fun transitionStatus(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val status = call.parameters["status"]?.let { TaskStatus.valueOf(it) }
            ?: throw DomainException.ValidationError("Status is required")

        val result = taskStatusService.transitionStatus(taskId, status)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun validateTransition(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val status = call.parameters["status"]?.let { TaskStatus.valueOf(it) }
            ?: throw DomainException.ValidationError("Status is required")

        val result = taskStatusService.validateStatusTransition(taskId, status)
        call.respond(HttpStatusCode.OK, mapOf("valid" to result))
    }

    suspend fun start(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")

        val result = taskStatusService.startTask(taskId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun complete(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")

        val result = taskStatusService.completeTask(taskId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun cancel(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")

        val result = taskStatusService.cancelTask(taskId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun reopen(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")

        val result = taskStatusService.reopenTask(taskId)
        call.respond(HttpStatusCode.OK, result)
    }
}