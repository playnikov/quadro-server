package com.quadro.team.infrastructure.database.repositories

import com.quadro.team.domain.models.TeamProjectBinding
import com.quadro.team.domain.repositories.TeamProjectBindingRepository
import com.quadro.team.infrastructure.database.entities.TeamMembersEntity
import com.quadro.team.infrastructure.database.entities.TeamMembersTable
import com.quadro.team.infrastructure.database.entities.TeamProjectsEntity
import com.quadro.team.infrastructure.database.entities.TeamProjectsTable
import com.quadro.team.infrastructure.database.mappers.TeamProjectsMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TeamProjectBindingRepositoryImpl : TeamProjectBindingRepository {
    override suspend fun findByTeam(teamId: UUID): List<TeamProjectBinding> = newSuspendedTransaction {
        TeamProjectsEntity.find { TeamProjectsTable.teamId eq teamId }
            .map { TeamProjectsMapper.toDomain(it) }
    }

    override suspend fun findByProject(projectId: UUID): List<TeamProjectBinding> = newSuspendedTransaction {
        TeamProjectsEntity.find { TeamProjectsTable.projectId eq projectId }
            .map { TeamProjectsMapper.toDomain(it) }
    }

    override suspend fun exists(teamId: UUID, projectId: UUID): Boolean = newSuspendedTransaction {
        !TeamProjectsEntity.find {
            (TeamProjectsTable.teamId eq teamId) and
                    (TeamProjectsTable.projectId eq projectId)
        }.empty()
    }

    override suspend fun bind(binding: TeamProjectBinding): TeamProjectBinding = newSuspendedTransaction {
        TeamProjectsMapper.toDomain(TeamProjectsMapper.toEntity(binding))
    }

    override suspend fun unbind(teamId: UUID, projectId: UUID): Boolean = newSuspendedTransaction {
        TeamProjectsEntity.find {
            (TeamProjectsTable.teamId eq teamId) and
                    (TeamProjectsTable.projectId eq projectId)
        }.firstOrNull()?.delete() != null
    }
}