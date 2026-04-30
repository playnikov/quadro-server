package com.quadro.team.presentation.controllers

import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.team.domain.models.TeamProjectRole
import com.quadro.team.domain.services.ProjectBindingService
import com.quadro.team.presentation.models.BindingRequest
import com.quadro.team.presentation.models.UnbindRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class ProjectBindingController(
    private val projectBindingService: ProjectBindingService
) {
    suspend fun bind(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val request = call.receive<BindingRequest>()
        val result = projectBindingService.bind(
            teamId = teamId,
            projectId = UUID.fromString(request.projectId),
            role = TeamProjectRole.valueOf(request.role),
            requesterId = userId
        )
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun unbind(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val request = call.receive<UnbindRequest>()
        val result = projectBindingService.unbind(
            teamId = teamId,
            projectId = UUID.fromString(request.projectId),
            requesterId = userId
        )
        call.respond(HttpStatusCode.NoContent)
    }

    suspend fun getBindingByTeam(call: ApplicationCall) {
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val result = projectBindingService.getBindingsByTeam(teamId)
        call.respond(HttpStatusCode.OK, result)
    }
}