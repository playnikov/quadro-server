package com.quadro.datasource.repositories.project

import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectTeam
import java.util.UUID

interface ProjectTeamRepository {
    suspend fun assign(projectTeam: ProjectTeam): ProjectTeam
    suspend fun findById(id: UUID): ProjectTeam?
    suspend fun findByProject(projectId: UUID): List<ProjectTeam>
    suspend fun findByTeam(teamId: UUID): List<ProjectTeam>
    suspend fun findByProjectAndTeam(projectId: UUID, teamId: UUID): ProjectTeam?
    suspend fun findLeadTeam(projectId: UUID): ProjectTeam?

    suspend fun updateRole(id: UUID, role: ProjectRole): Boolean
    suspend fun updateLeadTeam(id: UUID, isLeadTeam: Boolean): Boolean
    suspend fun remove(id: UUID): Boolean
    suspend fun removeByProjectAndTeam(projectId: UUID, teamId: UUID): Boolean
    suspend fun removeAllByProject(projectId: UUID): Int

    suspend fun countByProject(projectId: UUID): Long
    suspend fun countByTeam(teamId: UUID): Long
    suspend fun exists(projectId: UUID, teamId: UUID): Boolean
}