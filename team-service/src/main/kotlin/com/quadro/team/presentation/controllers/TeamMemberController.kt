package com.quadro.team.presentation.controllers

import com.quadro.shared.dto.DomainException
import com.quadro.shared.security.getUserId
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.services.TeamMemberService
import com.quadro.team.presentation.models.AddMemberRequest
import com.quadro.team.presentation.models.RemoveMember
import com.quadro.team.presentation.models.UpdateMemberRole
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.util.UUID

class TeamMemberController(
    private val teamMemberService: TeamMemberService
) {
    suspend fun getMembers(call: ApplicationCall) {
        val teamId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val result = teamMemberService.getMembers(teamId)
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun addMember(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val teamId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val request = call.receive<AddMemberRequest>()
        val result = teamMemberService.addMember(
            teamId = teamId,
            userId = UUID.fromString(request.userId),
            role = TeamRole.valueOf(request.role),
            addedBy = userId
        )
        call.respond(HttpStatusCode.Created, result)
    }

    suspend fun removeMember(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val teamId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val request = call.receive<RemoveMember>()
        val result = teamMemberService.removeMember(
            teamId = teamId,
            memberId = UUID.fromString(request.userId),
            requesterId = userId
        )
        call.respond(HttpStatusCode.OK, result)
    }

    suspend fun changeRole(call: ApplicationCall) {
        val userId = call.getUserId()
            ?: throw DomainException.Forbidden("Not authorized")
        val teamId = call.parameters["id"]?.let { UUID.fromString(it) }
            ?: throw DomainException.ValidationError("Team ID is invalid")
        val request = call.receive<UpdateMemberRole>()
        val result = teamMemberService.changeRole(
            teamId = teamId,
            memberId = UUID.fromString(request.userId),
            role = TeamRole.valueOf(request.role),
            requesterId = userId
        )
        call.respond(HttpStatusCode.OK, result)
    }
}