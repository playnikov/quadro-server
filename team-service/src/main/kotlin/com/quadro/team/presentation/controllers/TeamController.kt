package com.quadro.team.presentation.controllers

import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.team.domain.models.TeamCreate
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamUpdate
import com.quadro.team.domain.models.TeamVisibility
import com.quadro.team.domain.services.TeamService
import com.quadro.team.presentation.models.TeamCreateRequest
import com.quadro.team.presentation.models.TeamUpdateRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class TeamController(
    private val teamService: TeamService
) {
    suspend fun createTeam(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val request = call.receive<TeamCreateRequest>()
        val teamCreate = TeamCreate(
            name = request.name,
            description = request.description,
            avatar = request.avatar,
            leadId = UUID.fromString(request.leadId),
            visibility = TeamVisibility.valueOf(request.visibility),
            initialMembers = request.initialMembers?.map {
                UUID.fromString(it)
            }
        )

        val result = teamService.create(userId, teamCreate)
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun getById(call: ApplicationCall) {
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")

        val result = teamService.getById(teamId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun update(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val request = call.receive<TeamUpdateRequest>()
        val teamUpdate = TeamUpdate(
            name = request.name,
            description = request.description,
            avatar = request.avatar,
            leadId = request.leadId,
            visibility = request.visibility?.let { TeamVisibility.valueOf(it) },
            status = request.status?.let { TeamStatus.valueOf(it) }
        )
        val result = teamService.update(teamId, teamUpdate, userId)
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun delete(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val teamId = call.parameters["teamId"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        teamService.delete(teamId, userId)
        call.respond(HttpStatusCode.NoContent)
    }
}