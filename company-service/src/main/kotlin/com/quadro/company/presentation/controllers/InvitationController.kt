package com.quadro.company.presentation.controllers

import com.quadro.company.domain.models.InvitationCreate
import com.quadro.company.domain.services.CompanyInvitationService
import com.quadro.company.presentation.models.CreateInvitationRequest
import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class InvitationController(
    private val companyInvitationService: CompanyInvitationService
) {
    suspend fun createInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        val request = call.receive<CreateInvitationRequest>()
        val invitationCreate = InvitationCreate(
            role = request.role,
            identifier = request.identifier,
            message = request.message,
            expiresInDays = request.expiresInDays
        )
        val result = companyInvitationService.createInvitation(companyId, userId, invitationCreate)
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun getInvitations(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        val result = companyInvitationService.getInvitations(companyId, userId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun acceptInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val token = call.parameters["token"] ?: throw DomainException.ValidationError("Token is invalid")
        val result = companyInvitationService.acceptInvitation(token, userId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun cancelInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")

        val invitationId = call.parameters["invitationId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Invitation ID is invalid")
        companyInvitationService.cancelInvitation(companyId, userId, invitationId)
        call.respond(HttpStatusCode.OK, mapOf("message" to "Invitation cancelled successfully"))
    }
}