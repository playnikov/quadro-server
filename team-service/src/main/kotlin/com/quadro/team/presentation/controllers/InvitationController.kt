package com.quadro.team.presentation.controllers

import com.quadro.company.domain.models.InvitationCreate
import com.quadro.company.domain.services.CompanyInvitationService
import com.quadro.company.presentation.models.CreateInvitationRequest
import com.quadro.shared.dto.ErrorResponse
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
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized,
            ErrorResponse("Not authenticated")
        )
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        try {
            val request = call.receive<CreateInvitationRequest>()
            val invitationCreate = InvitationCreate(
                teamId = if (!request.teamId.isNullOrBlank()) {
                    UUID.fromString(request.teamId)
                } else {
                    null
                },
                role = request.role,
                identifier = request.identifier,
                message = request.message,
                expiresInDays = request.expiresInDays
            )

            val result = companyInvitationService.createInvitation(companyId, userId, invitationCreate)
            result.fold(
                onSuccess = { invitation ->
                    call.respond(HttpStatusCode.Created, invitation)
                },
                onFailure = { error ->
                    when (error.message) {
                        "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                        else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                    }
                }
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun getInvitations(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized,
            ErrorResponse("Not authenticated")
        )
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        val result = companyInvitationService.getInvitations(companyId, userId)
        result.fold(
            onSuccess = { invitations ->
                call.respond(HttpStatusCode.OK, invitations)
            },
            onFailure = { error ->
                when (error.message) {
                    "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun acceptInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized,
            ErrorResponse("Not authenticated")
        )
        try {
            val token = call.parameters["token"]
                ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid token"))
                    return
                }
            val result = companyInvitationService.acceptInvitation(token, userId)
            result.fold(
                onSuccess = { company ->
                    call.respond(HttpStatusCode.OK, company)
                },
                onFailure = { error ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun cancelInvitation(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized,
            ErrorResponse("Not authenticated")
        )

        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))
                return
            }

        val invitationId = call.parameters["invitationId"]?.let { UUID.fromString(it) }
            ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid invitation ID"))
                return
            }

        val result = companyInvitationService.cancelInvitation(companyId, userId, invitationId)

        result.fold(
            onSuccess = {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Invitation cancelled successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }
}