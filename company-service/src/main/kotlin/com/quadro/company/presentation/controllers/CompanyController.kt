package com.quadro.company.presentation.controllers

import com.quadro.company.domain.models.CompanyCreate
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.models.CompanyUpdate
import com.quadro.company.domain.services.CompanyService
import com.quadro.company.presentation.models.CreateCompanyRequest
import com.quadro.company.presentation.models.UpdateCompanyRequest
import com.quadro.company.presentation.models.UpdateMemberRoleRequest
import com.quadro.shared.dto.ErrorResponse
import com.quadro.shared.security.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory
import java.util.UUID

class CompanyController(
    private val companyService: CompanyService
) {
    suspend fun createCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized,
            ErrorResponse("Not authenticated")
        )
        try {
            val request = call.receive<CreateCompanyRequest>()
            val companyCreate = CompanyCreate(
                name = request.name,
                description = request.description,
                logo = request.logo,
                website = request.website,
                email = request.email,
                phone = request.phone,
                address = request.address,
                taxId = request.taxId
            )
            val result = companyService.createCompany(userId, companyCreate)
            result.fold(
                onSuccess = { call.respond(HttpStatusCode.Created, it) },
                onFailure = { error -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Creation failed")) }
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format"))
        }
    }

    suspend fun getCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        val result = companyService.getCompany(companyId, userId)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.OK, it) },
            onFailure = { error ->
                when (error.message) {
                    "Company not found" -> call.respond(HttpStatusCode.NotFound, ErrorResponse(error.message!!))
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, ErrorResponse(error.message!!))
                    else -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Failed to get company"))
                }
            }
        )
    }

    suspend fun updateCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        try {
            val request = call.receive<UpdateCompanyRequest>()
            val companyUpdate = CompanyUpdate(
                name = request.name,
                description = request.description,
                logo = request.logo,
                website = request.website,
                email = request.email,
                phone = request.phone,
                address = request.address,
                taxId = request.taxId
            )
            val result = companyService.updateCompany(companyId, userId, companyUpdate)
            result.fold(
                onSuccess = { call.respond(HttpStatusCode.OK, it) },
                onFailure = { error ->
                    when (error.message) {
                        "Company not found" -> call.respond(HttpStatusCode.NotFound, ErrorResponse(error.message!!))
                        "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, ErrorResponse(error.message!!))
                        else -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Update failed"))
                    }
                }
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format"))
        }
    }

    suspend fun deleteCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        val result = companyService.deleteCompany(companyId, userId)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.OK, mapOf("message" to "Company deleted")) },
            onFailure = { error ->
                when (error.message) {
                    "Company not found" -> call.respond(HttpStatusCode.NotFound, ErrorResponse(error.message!!))
                    "Only owner can delete company" -> call.respond(HttpStatusCode.Forbidden, ErrorResponse(error.message!!))
                    else -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Delete failed"))
                }
            }
        )
    }

    suspend fun getUserCompanies(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
        val result = companyService.getUserCompanies(userId, page, size)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.OK, it) },
            onFailure = { error -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Failed to get user companies")) }
        )
    }

    suspend fun getCompanyMembers(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
        val result = companyService.getCompanyMembers(companyId, userId, page, size)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.OK, it) },
            onFailure = { error -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Failed to get members")) }
        )
    }

    suspend fun updateMemberRole(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        val targetUserId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
        try {
            val request = call.receive<UpdateMemberRoleRequest>()
            val role = try { CompanyRole.valueOf(request.role.uppercase()) } catch (e: Exception) {
                return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid role"))
            }
            val result = companyService.updateMemberRole(companyId, userId, targetUserId, role)
            result.fold(
                onSuccess = { call.respond(HttpStatusCode.OK, mapOf("message" to "Role updated")) },
                onFailure = { error -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Update failed")) }
            )
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format"))
        }
    }

    suspend fun removeMember(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        val targetUserId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid user ID"))
        val result = companyService.removeMember(companyId, userId, targetUserId)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.OK, mapOf("message" to "Member removed")) },
            onFailure = { error -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Remove failed")) }
        )
    }

    suspend fun leaveCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authenticated"))
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: return call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid company ID"))
        val result = companyService.leaveCompany(companyId, userId)
        result.fold(
            onSuccess = { call.respond(HttpStatusCode.OK, mapOf("message" to "Left company")) },
            onFailure = { error -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(error.message ?: "Leave failed")) }
        )
    }
}