package com.quadro.task.presentation.controllers

import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.task.domain.models.task.SprintCreate
import com.quadro.task.domain.models.task.SprintUpdate
import com.quadro.task.domain.services.SprintService
import com.quadro.task.presentation.models.SprintCreateRequest
import com.quadro.task.presentation.models.SprintResponse
import com.quadro.task.presentation.models.SprintUpdateRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class SprintController(
    private val sprintService: SprintService
) {
    suspend fun createSprint(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val request = call.receive<SprintCreateRequest>()
        val sprintCreate = SprintCreate(
            projectId = UUID.fromString(request.projectId),
            name = request.name,
            goal = request.goal,
            status = request.status,
            startDate = request.startDate,
            endDate = request.endDate,
            createdBy = userId
        )

        val result = sprintService.createSprint(sprintCreate)
        call.respond(HttpStatusCode.Created, ApiResponse.ok(SprintResponse.from(result)))
    }

    suspend fun updateSprint(call: ApplicationCall) {
        val sprintId = call.parameters["sprintId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Sprint ID is invalid")
        val request = call.receive<SprintUpdateRequest>()
        val sprintUpdate = SprintUpdate(
            name = request.name,
            goal = request.goal,
            status = request.status,
            startDate = request.startDate,
            endDate = request.endDate
        )

        val result = sprintService.updateSprint(sprintId, sprintUpdate)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(SprintResponse.from(result)))
    }

    suspend fun deleteSprint(call: ApplicationCall) {
        val sprintId = call.parameters["sprintId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Sprint ID is invalid")
        sprintService.deleteSprint(sprintId)
        call.respond(HttpStatusCode.NoContent, ApiResponse.ok("Deleted sprint: $sprintId"))
    }

    suspend fun findById(call: ApplicationCall) {
        val sprintId = call.parameters["sprintId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Sprint ID is invalid")

        val result = sprintService.getSprint(sprintId)
        if (result == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(HttpStatusCode.OK, ApiResponse.ok(SprintResponse.from(result)))
        }
    }

    suspend fun findByProject(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val sprints = sprintService.getSprintsByProject(projectId)
            .map(SprintResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(sprints))
    }
}