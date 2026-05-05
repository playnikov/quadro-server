package com.quadro.task.domain.repositories.team

import com.quadro.task.domain.models.team.TeamProject
import java.util.UUID

interface TeamProjectRepository {
    suspend fun upsert(teamProject: TeamProject)
    suspend fun findByTeamAndProject(teamId: UUID, projectId: UUID): TeamProject?
    suspend fun findByTeamId(teamId: UUID): List<TeamProject>
    suspend fun findByProjectId(projectId: UUID): List<TeamProject>
    suspend fun delete(teamId: UUID, projectId: UUID)
}