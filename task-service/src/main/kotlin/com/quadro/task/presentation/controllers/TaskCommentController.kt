package com.quadro.task.presentation.controllers

import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.task.domain.models.task.TaskCommentCreate
import com.quadro.task.domain.models.task.TaskCommentUpdate
import com.quadro.task.domain.services.TaskCommentService
import com.quadro.task.presentation.models.TaskCommentCreateRequest
import com.quadro.task.presentation.models.TaskCommentResponse
import com.quadro.task.presentation.models.TaskCommentUpdateRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class TaskCommentController(
    private val commentService: TaskCommentService
) {
    suspend fun createComment(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val request = call.receive<TaskCommentCreateRequest>()
        val commentCreate = TaskCommentCreate(
            taskId = UUID.fromString(request.taskId),
            authorId = userId,
            content = request.content,
            parentId = request.parentId?.let { UUID.fromString(it) },
            mentions = request.mentions?.map { UUID.fromString(it) } ?: emptyList()
        )
        val comment = commentService.createComment(commentCreate)
        call.respond(HttpStatusCode.Created, ApiResponse.ok(TaskCommentResponse.from(comment)))
    }

    suspend fun updateComment(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val commentId = call.parameters["commentId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Comment ID is invalid")
        val request = call.receive<TaskCommentUpdateRequest>()
        val update = TaskCommentUpdate(
            content = request.content
        )
        val comment = commentService.updateComment(commentId, userId, update)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(TaskCommentResponse.from(comment)))
    }

    suspend fun deleteComment(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val commentId = call.parameters["commentId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Comment ID is invalid")
        commentService.deleteComment(commentId, userId)
        call.respond(HttpStatusCode.NoContent, ApiResponse.ok(mapOf("message" to "Comment deleted")))
    }

    suspend fun findById(call: ApplicationCall) {
        val commentId = call.parameters["commentId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Comment ID is invalid")
        val comment = commentService.getComment(commentId)
        if (comment == null) {
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(HttpStatusCode.OK, ApiResponse.ok(TaskCommentResponse.from(comment)))
        }
    }

    suspend fun findByTask(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val comments = commentService.getCommentsByTask(taskId)
            .map(TaskCommentResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(comments))
    }

    suspend fun findReplies(call: ApplicationCall) {
        val parentId = call.parameters["parentId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Parent comment ID is invalid")
        val replies = commentService.getReplies(parentId)
            .map(TaskCommentResponse::from)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(replies))
    }

    suspend fun countByTask(call: ApplicationCall) {
        val taskId = call.parameters["taskId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Task ID is invalid")
        val count = commentService.countByTask(taskId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("count" to count)))
    }
}