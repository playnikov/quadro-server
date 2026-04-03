package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.TeamProject
import java.util.UUID

interface TeamProjectRepository {
    suspend fun assign(teamProject: TeamProject): TeamProject
    suspend fun findById(id: UUID): TeamProject?
    suspend fun findByTeam(teamId: UUID): List<TeamProject>
    suspend fun findByProject(projectId: UUID): List<TeamProject>
    suspend fun findByTeamAndProject(teamId: UUID, projectId: UUID): TeamProject?
    suspend fun remove(id: UUID): Boolean
    suspend fun removeByTeamAndProject(teamId: UUID, projectId: UUID): Boolean
    suspend fun countByTeam(teamId: UUID): Long
    suspend fun countByProject(projectId: UUID): Long
    suspend fun exists(teamId: UUID, projectId: UUID): Boolean
}