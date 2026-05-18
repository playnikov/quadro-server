package com.quadro.task.presentation.controllers

import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.services.TaskReportingService
import com.quadro.task.presentation.models.TaskResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class TaskReportingController(
    private val taskReportingService: TaskReportingService
) {
    suspend fun getBacklogCount(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val count = taskReportingService.getBacklogCount(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("count" to count)))
    }

    suspend fun getTodoCount(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val count = taskReportingService.getTodoCount(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("count" to count)))
    }

    suspend fun getInProgressCount(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val count = taskReportingService.getInProgressCount(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("count" to count)))
    }

    suspend fun getInReviewCount(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val count = taskReportingService.getInReviewCount(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("count" to count)))
    }

    suspend fun getDoneCount(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val count = taskReportingService.getDoneCount(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("count" to count)))
    }

    suspend fun getCancelledCount(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val count = taskReportingService.getCancelledCount(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("count" to count)))
    }

    suspend fun getTaskCounts(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val counts = taskReportingService.getTaskCounts(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(counts))
    }

    suspend fun getOverdueTasks(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val now = Clock.System.now()

        val tasks = taskReportingService.getOverdueTasks(projectId, now)
        tasks.forEach {
            TaskResponse.from(it)
        }
        call.respond(HttpStatusCode.OK, ApiResponse.ok(tasks))
    }

    suspend fun getAverageCompletionDays(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val days = taskReportingService.getAverageCompletionDays(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("days" to days)))
    }

    suspend fun getCompletionRate(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val rate = taskReportingService.getCompletionRate(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("rate" to rate)))
    }

    suspend fun getVelocity(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val velocity = taskReportingService.getVelocity(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("velocity" to velocity)))
    }

    suspend fun getReporting(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val from = Instant.parse(call.request.queryParameters["from"] ?: throw DomainException.ValidationError("Missing 'from'"))
        val to = Instant.parse(call.request.queryParameters["to"] ?: throw DomainException.ValidationError("Missing 'to'"))
        val report = taskReportingService.getPeriodReport(projectId, from, to)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(report))
    }
}