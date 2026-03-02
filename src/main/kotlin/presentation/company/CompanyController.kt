package com.quadro.presentation.company

import com.quadro.domain.models.CompanyCreate
import com.quadro.domain.models.CompanyUpdate
import com.quadro.domain.services.CompanyInvitationService
import com.quadro.domain.services.CompanyService
import com.quadro.plugins.getUserId
import com.quadro.presentation.company.models.CompanyResponse
import com.quadro.presentation.company.models.CreateCompanyRequest
import com.quadro.presentation.company.models.CreateInvitationRequest
import com.quadro.presentation.company.models.InvitationResponse
import com.quadro.presentation.company.models.UpdateCompanyRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class CompanyController(
    private val companyService: CompanyService,
    private val companyInvitationService: CompanyInvitationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // ============== Company CRUD ==============

    suspend fun createCompany(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.BadRequest, message = "${call.getUserId()}")

        try {
            val request = call.receive<CreateCompanyRequest>()
            logger.info("[$requestId] Creating company: ${request.name} by user: $userId")

            val userCreate = CompanyCreate(
                name = request.name,
                description = request.description,
                logo = request.logo,
                website = request.website,
                email = request.email,
                phone = request.phone,
                address = request.address,
                taxId = request.taxId
            )

            val result = companyService.createCompany(userId, userCreate)

            result.fold(
                onSuccess = { company ->
                    logger.info("[$requestId] Company created successfully: ${company.id}")
                    call.respond(HttpStatusCode.Created, CompanyResponse.fromCompany(company))
                },
                onFailure = { error ->
                    logger.warn("[$requestId] Company creation failed: ${error.message}")
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Company creation error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun getCompany(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.BadRequest, message = "${call.getUserId()}")

        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))
                return
            }

        val result = companyService.getCompany(userId, companyId)
        result.fold(
            onSuccess = { company ->
                call.respond(HttpStatusCode.OK, CompanyResponse.fromCompany(company))
            },
            onFailure = { error ->
                when (error.message) {
                    "Company not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Access denied" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun updateCompany(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.BadRequest, message = "${call.getUserId()}")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))
                return
            }

        try {
            val request = call.receive<UpdateCompanyRequest>()

            val updateCompany = CompanyUpdate(
                name = request.name,
                description = request.description,
                logo = request.logo,
                website = request.website,
                email = request.email,
                phone = request.phone,
                address = request.address,
                taxId = request.taxId
            )

            val result = companyService.updateCompany(companyId, userId, updateCompany)
            result.fold(
                onSuccess = { company ->
                    logger.info("[$requestId] Company updated: ${company.id}")
                    call.respond(HttpStatusCode.OK, CompanyResponse.fromCompany(company))
                },
                onFailure = { error ->
                    when (error.message) {
                        "Company not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                        "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                        else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                    }
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Company update error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun deleteCompany(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.BadRequest, message = "${call.getUserId()}")
        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))
                return
            }

        val result = companyService.deleteCompany(companyId, userId)

        result.fold(
            onSuccess = {
                logger.info("[$requestId] Company deleted: $companyId")
                call.respond(HttpStatusCode.OK, mapOf("message" to "Company deleted successfully"))
            },
            onFailure = { error ->
                when (error.message) {
                    "Company not found" -> call.respond(HttpStatusCode.NotFound, mapOf("error" to error.message))
                    "Only owner can delete company" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun getUserCompanies(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.BadRequest, message = "${call.getUserId()}")
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 20

        val result = companyService.getUserCompanies(userId, page, size)

        result.fold(
            onSuccess = { companies ->
                call.respond(HttpStatusCode.OK, companies.map { CompanyResponse.fromCompany(it) })
            },
            onFailure = { error ->
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
            }
        )
    }

    // ============== Invitations ==============

    suspend fun createInvitation(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))
                return
            }

        try {
            val request = call.receive<CreateInvitationRequest>()
            logger.info("[$requestId] Creating invitation for company: $companyId")

            val invitationCreate = com.quadro.domain.models.InvitationCreate(
                teamId = if (!request.teamId.isNullOrBlank()) {
                    UUID.fromString(request.teamId)
                } else {
                    null
                },
                role = try {
                    com.quadro.domain.models.CompanyRole.valueOf(request.role.uppercase())
                } catch (e: Exception) {
                    com.quadro.domain.models.CompanyRole.MEMBER
                },
                message = request.message,
                expiresInDays = request.expiresInDays
            )

            val result = companyInvitationService.createInvitation(companyId, userId, invitationCreate)

            result.fold(
                onSuccess = { invitation ->
                    logger.info("[$requestId] Invitation created successfully")
                    call.respond(HttpStatusCode.Created, InvitationResponse.fromInvitationResult(invitation))
                },
                onFailure = { error ->
                    logger.warn("[$requestId] Invitation creation failed: ${error.message}")
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Invitation creation error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }

    suspend fun getInvitations(call: ApplicationCall) {
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

        val companyId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid company ID"))
                return
            }

        val result = companyInvitationService.getInvitations(companyId, userId)

        result.fold(
            onSuccess = { invitations ->
                call.respond(HttpStatusCode.OK, invitations.map { InvitationResponse.fromInvitationResult(it) })
            },
            onFailure = { error ->
                when (error.message) {
                    "Insufficient permissions" -> call.respond(HttpStatusCode.Forbidden, mapOf("error" to error.message))
                    else -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            }
        )
    }

    suspend fun cancelInvitation(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

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
                logger.info("[$requestId] Invitation cancelled: $invitationId")
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

    suspend fun resendInvitation(call: ApplicationCall) {
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

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

        val result = companyInvitationService.resendInvitation(companyId, userId, invitationId)

        result.fold(
            onSuccess = { invitation ->
                logger.info("[$requestId] Invitation resent: $invitationId")
                call.respond(HttpStatusCode.OK, InvitationResponse.fromInvitationResult(invitation))
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
        val requestId = call.request.headers["X-Request-ID"] ?: UUID.randomUUID().toString()
        val userId = call.getUserId() ?: return call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not authenticated"))

        try {
            val token = call.parameters["token"]
                ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid token"))
                    return
                }
            logger.info("[$requestId] Accepting invitation with token")

            val result = companyInvitationService.acceptInvitation(userId, token)

            result.fold(
                onSuccess = { company ->
                    logger.info("[$requestId] Invitation accepted successfully")
                    call.respond(HttpStatusCode.OK, CompanyResponse.fromCompany(company))
                },
                onFailure = { error ->
                    logger.warn("[$requestId] Invitation acceptance failed: ${error.message}")
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to error.message))
                }
            )
        } catch (e: Exception) {
            logger.error("[$requestId] Accept invitation error", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
        }
    }
}