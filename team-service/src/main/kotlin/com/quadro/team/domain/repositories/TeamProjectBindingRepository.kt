package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.models.TeamProjectRole
import java.util.UUID

interface TeamProjectBindingRepository {
    suspend fun findByTeam(teamId: UUID): List<TeamProjectBinding>
    suspend fun findByProject(projectId: UUID): List<TeamProjectBinding>
    suspend fun exists(teamId: UUID, projectId: UUID): Boolean
    suspend fun bind(binding: TeamProjectBinding): TeamProjectBinding
    suspend fun unbind(teamId: UUID, projectId: UUID): Boolean
}