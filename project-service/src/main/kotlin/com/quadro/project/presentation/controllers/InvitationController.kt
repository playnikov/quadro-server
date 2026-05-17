package com.quadro.project.presentation.controllers

import com.quadro.project.domain.models.InvitationCreate
import com.quadro.project.domain.services.ProjectInvitationService
import com.quadro.project.presentation.models.CreateInvitationRequest
import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class InvitationController(
    private val projectInvitationService: ProjectInvitationService
) {
    suspend fun createInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val projectId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val request = call.receive<CreateInvitationRequest>()
        val invitationCreate = InvitationCreate(
            role = request.role,
            identifier = request.identifier,
            message = request.message,
            expiresInDays = request.expiresInDays
        )
        val result = projectInvitationService.createInvitation(projectId, userId, invitationCreate)
        call.respond(HttpStatusCode.Created, ApiResponse.ok(result))
    }

    suspend fun getInvitations(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val projectId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val result = projectInvitationService.getInvitations(projectId, userId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(result))
    }

    suspend fun getInvitationsByEmail(call: ApplicationCall) {
        val email = call.parameters["email"]
            ?: throw DomainException.ValidationError("Email is invalid")
        val result = projectInvitationService.getInvitationsByEmail(email)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(result))
    }

    suspend fun acceptInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val token = call.parameters["token"] ?: throw DomainException.ValidationError("Token is invalid")
        val result = projectInvitationService.acceptInvitation(token, userId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(result))
    }

    suspend fun cancelInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val projectId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Project ID is invalid")
        val invitationId = call.parameters["invitationId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Invitation ID is invalid")
        projectInvitationService.cancelInvitation(projectId, userId, invitationId)
        call.respond(HttpStatusCode.OK, ApiResponse.ok(mapOf("message" to "Invitation cancelled successfully")))
    }
}