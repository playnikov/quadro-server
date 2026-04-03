package com.quadro.datasource.repositories.project

import com.quadro.datasource.entities.ProjectTeamEntity
import com.quadro.datasource.entities.ProjectTeamsTable
import com.quadro.datasource.mappers.ProjectTeamMapper
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectTeam
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ProjectTeamRepositoryImpl : ProjectTeamRepository {
    override suspend fun assign(projectTeam: ProjectTeam): ProjectTeam = newSuspendedTransaction {
        ProjectTeamMapper.toDomain(ProjectTeamMapper.toEntity(projectTeam))
    }

    override suspend fun findById(id: UUID): ProjectTeam? = newSuspendedTransaction {
        ProjectTeamEntity.findById(id)?.let { ProjectTeamMapper.toDomain(it) }
    }

    override suspend fun findByProject(projectId: UUID): List<ProjectTeam> = newSuspendedTransaction {
        ProjectTeamEntity.find { ProjectTeamsTable.projectId eq projectId }
            .map { ProjectTeamMapper.toDomain(it) }
    }

    override suspend fun findByTeam(teamId: UUID): List<ProjectTeam> = newSuspendedTransaction {
        ProjectTeamEntity.find { ProjectTeamsTable.teamId eq teamId }
            .map { ProjectTeamMapper.toDomain(it) }
    }

    override suspend fun findByProjectAndTeam(
        projectId: UUID,
        teamId: UUID
    ): ProjectTeam? = newSuspendedTransaction {
        ProjectTeamEntity.find {
            (ProjectTeamsTable.projectId eq projectId) and
                    (ProjectTeamsTable.teamId eq teamId)
        }.firstOrNull()?.let { ProjectTeamMapper.toDomain(it) }
    }

    override suspend fun findLeadTeam(projectId: UUID): ProjectTeam? = newSuspendedTransaction {
        ProjectTeamEntity.find {
            (ProjectTeamsTable.projectId eq projectId) and
                    (ProjectTeamsTable.isLeadTeam eq true)
        }.firstOrNull()?.let { ProjectTeamMapper.toDomain(it) }
    }

    override suspend fun updateRole(
        id: UUID,
        role: ProjectRole
    ): Boolean = newSuspendedTransaction {
        ProjectTeamEntity.findById(id)?.apply {
            this.role = role.name
        } != null
    }

    override suspend fun updateLeadTeam(id: UUID, isLeadTeam: Boolean): Boolean = newSuspendedTransaction {
        ProjectTeamEntity.findById(id)?.apply {
            this.isLeadTeam = isLeadTeam
        } != null
    }

    override suspend fun remove(id: UUID): Boolean  = newSuspendedTransaction {
        ProjectTeamEntity.findById(id)?.delete() != null
    }

    override suspend fun removeByProjectAndTeam(projectId: UUID, teamId: UUID): Boolean = newSuspendedTransaction {
        val relation = findByProjectAndTeam(projectId, teamId)
        if (relation != null) {
            ProjectTeamEntity.findById(relation.id)?.delete() != null
        } else false
    }

    override suspend fun removeAllByProject(projectId: UUID): Int = newSuspendedTransaction {
        val relations = ProjectTeamEntity.find { ProjectTeamsTable.projectId eq projectId }.toList()
        relations.forEach { it.delete() }
        relations.size
    }

    override suspend fun countByProject(projectId: UUID): Long = newSuspendedTransaction {
        ProjectTeamEntity.find { ProjectTeamsTable.projectId eq projectId }.count()
    }

    override suspend fun countByTeam(teamId: UUID): Long = newSuspendedTransaction {
        ProjectTeamEntity.find { ProjectTeamsTable.teamId eq teamId }.count()
    }

    override suspend fun exists(projectId: UUID, teamId: UUID): Boolean = newSuspendedTransaction {
        !ProjectTeamEntity.find {
            (ProjectTeamsTable.projectId eq projectId) and
                    (ProjectTeamsTable.teamId eq teamId)
        }.empty()
    }
}