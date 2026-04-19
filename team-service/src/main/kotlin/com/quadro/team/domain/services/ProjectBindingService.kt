package com.quadro.team.domain.services

import com.quadro.team.domain.models.TeamProjectBindingResponse
import com.quadro.team.domain.models.TeamProjectRole
import java.util.UUID

interface ProjectBindingService {
    suspend fun bind(teamId: UUID, projectId: UUID, role: TeamProjectRole, requesterId: UUID): TeamProjectBindingResponse
    suspend fun unbind(teamId: UUID, projectId: UUID, requesterId: UUID)
    suspend fun getBindingsByTeam(teamId: UUID): List<TeamProjectBindingResponse>
}