package com.quadro.task.presentation.controllers

import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.task.domain.models.task.Task
import com.quadro.task.domain.models.task.TaskCreate
import com.quadro.task.domain.models.task.TaskHistory
import com.quadro.task.domain.models.task.TaskHistoryResponse
import com.quadro.task.domain.models.task.TaskStatus
import com.quadro.task.domain.models.task.TaskUpdate
import com.quadro.task.domain.services.TaskHistoryService
import com.quadro.task.domain.services.TaskService
import com.quadro.task.presentation.models.TaskCreateRequest
import com.quadro.task.presentation.models.TaskResponse
import com.quadro.task.presentation.models.TaskUpdateRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID
import kotlin.time.Clock

class TaskController(
    private val taskService: TaskService,
    private val taskHistoryService: TaskHistoryService
) {
    suspend fun getHistory(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
        val offset = call.parameters["offset"]?.toIntOrNull() ?: 0
        val result = taskHistoryService.getHistory(taskId, limit, offset)
            .map(TaskHistoryResponse::from)
        call.respond(HttpStatusCode.Created, ApiResponse.ok(result))
    }

    suspend fun createTask(call: ApplicationCall) {
        val reporterId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val request = call.receive<TaskCreateRequest>()
        val taskCreate = TaskCreate(
            title = request.title,
            projectId = UUID.fromString(request.projectId),
            description = request.description,
            priority = request.priority,
            type = request.type,
            assigneeId = request.assigneeId?.let { UUID.fromString(request.assigneeId) },
            sprintId = request.sprintId?.let { UUID.fromString(request.sprintId) },
            parentTaskId = request.parentTaskId?.let { UUID.fromString(request.parentTaskId) },
            storyPoints = request.storyPoints,
            estimatedHours = request.estimatedHours,
            dueDate = request.dueDate,
            labels = request.labels
        )

        val result = taskService.createTask(taskCreate, reporterId)
        call.respond(HttpStatusCode.Created, ApiResponse.ok(TaskResponse.from(result)))
    }

    suspend fun updateTask(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val request = call.receive<TaskUpdateRequest>()
        val taskUpdate = TaskUpdate(
            title = request.title,
            description = request.description,
            status = request.status?.let { TaskStatus.valueOf(it) },
            priority = request.priority?.let { com.quadro.task.domain.models.task.TaskPriority.valueOf(it) },
            assigneeId = request.assigneeId?.let { UUID.fromString(it) },
            sprintId = request.sprintId?.let { UUID.fromString(it) },
            storyPoints = request.storyPoints,
            estimatedHours = request.estimatedHours,
            loggedHours = request.loggedHours,
            dueDate = request.dueDate,
            labels = request.labels
        )

        val result = taskService.updateTask(userId, taskId, taskUpdate)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(TaskResponse.from(result)))
    }

    suspend fun deleteTask(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        taskService.deleteTask(taskId)
        call.respond(HttpStatusCode.NoContent, ApiResponse.ok("Deleted task: $taskId"))
    }

    suspend fun findById(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")

        val result = taskService.getTask(taskId)
        if (result == null) {
            call.respond(HttpStatusCode.NotFound)
            return
        }
        call.respond(HttpStatusCode.OK, ApiResponse.ok(TaskResponse.from(result)))
    }

    suspend fun findByProject(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
        val offset = call.parameters["offset"]?.toIntOrNull() ?: 0

        val tasks = taskService.getTasksByProject(projectId, limit, offset)
            .map(TaskResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(tasks))
    }

    suspend fun findBySprint(call: ApplicationCall) {
        val sprintId = call.parameters["sprintId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Sprint ID is invalid")

        val tasks = taskService.getTasksBySprint(sprintId)
            .map(TaskResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(tasks))
    }

    suspend fun findByAssignee(call: ApplicationCall) {
        val userId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("User ID is invalid")

        val tasks = taskService.getTasksByAssignee(userId)
            .map(TaskResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(tasks))
    }

    suspend fun findByParent(call: ApplicationCall) {
        val parentTaskId = call.parameters["parentTaskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Parent task ID is invalid")

        val tasks = taskService.getTasksByParent(parentTaskId)
            .map(TaskResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(tasks))
    }

    suspend fun getUpcomingDeadlines(call: ApplicationCall) {
        val projectId = call.parameters["projectId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val tasks = taskService.getUpcomingDeadlines(projectId, 7)
            .map(TaskResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(tasks))
    }
}