package com.quadro.company.presentation.controllers

import com.quadro.company.domain.models.CompanyCreate
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.models.CompanyUpdate
import com.quadro.company.domain.services.CompanyService
import com.quadro.company.presentation.models.CreateCompanyRequest
import com.quadro.company.presentation.models.UpdateCompanyRequest
import com.quadro.company.presentation.models.UpdateMemberRoleRequest
import com.quadro.shared.dto.DomainException
import com.quadro.shared.dto.ApiResponse
import com.quadro.shared.security.getUserId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class CompanyController(
    private val companyService: CompanyService
) {
    suspend fun createCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
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
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun getCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        val result = companyService.getCompany(companyId, userId)
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun updateCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")

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
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun deleteCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        companyService.deleteCompany(companyId, userId)
        call.respond(HttpStatusCode.OK, mapOf("message" to "Company deleted"))
    }

    suspend fun getUserCompanies(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
        val result = companyService.getUserCompanies(userId, page, size)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun getCompanyMembers(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20
        val result = companyService.getCompanyMembers(companyId, userId, page, size)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun updateMemberRole(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        val targetUserId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Target user ID is invalid")
        val request = call.receive<UpdateMemberRoleRequest>()
        val role = CompanyRole.valueOf(request.role.uppercase())
        companyService.updateMemberRole(companyId, userId, targetUserId, role)
        call.respond(HttpStatusCode.OK, mapOf("message" to "Role updated"))
    }

    suspend fun removeMember(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        val targetUserId = call.parameters["userId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Target user ID is invalid")
        companyService.removeMember(companyId, userId, targetUserId)
        call.respond(HttpStatusCode.OK, mapOf("message" to "Member removed"))
    }

    suspend fun leaveCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: throw DomainException.Forbidden("Not authorized")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Company ID is invalid")
        companyService.leaveCompany(companyId, userId)
        call.respond(HttpStatusCode.OK, mapOf("message" to "Company leaved successfully"))
    }
}