package com.quadro.task.infrastructure.database.repositories.team

import com.quadro.task.domain.models.team.TeamProject
import com.quadro.task.domain.repositories.team.TeamProjectRepository
import com.quadro.task.infrastructure.database.entities.team.TeamProjectsEntity
import com.quadro.task.infrastructure.database.entities.team.TeamProjectsTable
import com.quadro.task.infrastructure.database.mappers.team.TeamProjectMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TeamProjectRepositoryImpl : TeamProjectRepository {
    override suspend fun upsert(teamProject: TeamProject): Unit = newSuspendedTransaction {
        val existing = TeamProjectsEntity.find {
            (TeamProjectsTable.teamId eq teamProject.teamId) and
                    (TeamProjectsTable.projectId eq teamProject.projectId)
        }.firstOrNull()
        if (existing != null) {
            TeamProjectMapper.updateEntity(existing, teamProject)
        } else {
            TeamProjectMapper.toEntity(teamProject)
        }
    }

    override suspend fun findByTeamAndProject(
        teamId: UUID,
        projectId: UUID
    ): TeamProject? = newSuspendedTransaction {
        TeamProjectsEntity.find {
            (TeamProjectsTable.teamId eq teamId) and
                    (TeamProjectsTable.projectId eq projectId)
        }.firstOrNull()?.let(TeamProjectMapper::toDomain)
    }

    override suspend fun findByTeamId(teamId: UUID): List<TeamProject> = newSuspendedTransaction {
        TeamProjectsEntity.find { TeamProjectsTable.teamId eq teamId }
            .map(TeamProjectMapper::toDomain)
    }

    override suspend fun findByProjectId(projectId: UUID): List<TeamProject> = newSuspendedTransaction {
        TeamProjectsEntity.find { TeamProjectsTable.projectId eq projectId }
            .map(TeamProjectMapper::toDomain)
    }

    override suspend fun delete(teamId: UUID, projectId: UUID): Unit = newSuspendedTransaction {
        TeamProjectsEntity.find {
            (TeamProjectsTable.teamId eq teamId) and
                    (TeamProjectsTable.projectId eq projectId)
        }.firstOrNull()?.delete()
    }
}