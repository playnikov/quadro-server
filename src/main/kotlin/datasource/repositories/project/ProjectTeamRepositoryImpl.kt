package com.quadro.datasource.repositories.project

import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectTeam
import java.util.UUID

class ProjectTeamRepositoryImpl : ProjectTeamRepository {
    override suspend fun assign(projectTeam: ProjectTeam): ProjectTeam {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: UUID): ProjectTeam? {
        TODO("Not yet implemented")
    }

    override suspend fun findByProject(projectId: UUID): List<ProjectTeam> {
        TODO("Not yet implemented")
    }

    override suspend fun findByTeam(teamId: UUID): List<ProjectTeam> {
        TODO("Not yet implemented")
    }

    override suspend fun findByProjectAndTeam(
        projectId: UUID,
        teamId: UUID
    ): ProjectTeam? {
        TODO("Not yet implemented")
    }

    override suspend fun findLeadTeam(projectId: UUID): ProjectTeam? {
        TODO("Not yet implemented")
    }

    override suspend fun updateRole(
        id: UUID,
        role: ProjectRole
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateLeadTeam(id: UUID, isLeadTeam: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun remove(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removeByProjectAndTeam(projectId: UUID, teamId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removeAllByProject(projectId: UUID): Int {
        TODO("Not yet implemented")
    }

    override suspend fun countByProject(projectId: UUID): Long {
        TODO("Not yet implemented")
    }

    override suspend fun countByTeam(teamId: UUID): Long {
        TODO("Not yet implemented")
    }

    override suspend fun exists(projectId: UUID, teamId: UUID): Boolean {
        TODO("Not yet implemented")
    }
}