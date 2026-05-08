package com.quadro.project.presentation.controllers

import com.quadro.project.domain.models.*
import com.quadro.project.domain.services.ProjectService
import com.quadro.project.presentation.models.*
import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID
import kotlin.time.Clock

class ProjectController(
    private val projectService: ProjectService
) {
    suspend fun createProject(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val request = call.receive<ProjectCreateRequest>()
        val projectCreate = ProjectCreate(
            type = ProjectType.valueOf(request.type),
            name = request.name,
            key = request.key,
            description = request.description,
            priority = ProjectPriority.valueOf(request.priority),
            visibility = ProjectVisibility.valueOf(request.visibility)
        )

        val result = projectService.createProject(userId, projectCreate)
        call.respond(HttpStatusCode.Created, ApiResponse.ok(ProjectResponse.from(result)))
    }

    suspend fun updateProject(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val request = call.receive<ProjectUpdateRequest>()
        val projectUpdate = ProjectUpdate(
            name = request.name,
            description = request.description,
            status = request.status?.let { ProjectStatus.valueOf(it) },
            priority = request.priority?.let { ProjectPriority.valueOf(it) },
            visibility = request.visibility?.let { ProjectVisibility.valueOf(it) }
        )

        val result = projectService.updateProject(userId, projectId, projectUpdate)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(ProjectResponse.from(result)))
    }

    suspend fun deleteProject(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        projectService.deleteProject(userId, projectId)
        call.respond(HttpStatusCode.NoContent, ApiResponse.ok("Deleted project $projectId"))
    }

    suspend fun findById(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")

        val result = projectService.findById(projectId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(ProjectResponse.from(result)))
    }

    suspend fun findByName(call: ApplicationCall) {
        val name = call.parameters["name"] ?: throw DomainException.ValidationError("Name is required")

        val result = projectService.findByName(name)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(ProjectResponse.from(result)))
    }

    suspend fun findByUser(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
        val offset = call.parameters["offset"]?.toIntOrNull() ?: 0

        val projects = projectService.findByUser(userId, limit, offset)
        val result = projects.map { project ->
            ProjectResponse.from(project)
        }
        call.respond(HttpStatusCode.OK, ApiResponse.ok(result))
    }

    suspend fun updateStatus(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val status = call.parameters["status"]?.let { ProjectStatus.valueOf(it) }
            ?: throw DomainException.ValidationError("Status is required")

        val result = projectService.updateStatus(userId, projectId, status)
        if (result) {
            call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("success" to true)))
        } else {
            throw DomainException.NotFound("Project", "Project Not Found")
        }
    }
}