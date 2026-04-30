package com.quadro.team.infrastructure.database.repositories

import com.quadro.team.domain.models.Team
import com.quadro.team.domain.repositories.TeamRepository
import com.quadro.team.infrastructure.database.entities.TeamEntity
import com.quadro.team.infrastructure.database.entities.TeamsTable
import com.quadro.team.infrastructure.database.mappers.TeamMapper
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TeamRepositoryImpl : TeamRepository {
    override suspend fun findById(id: UUID): Team? = newSuspendedTransaction {
        TeamEntity.findById(id)?.let { TeamMapper.toDomain(it) }
    }

    override suspend fun findAll(page: Int, size: Int): List<Team> = newSuspendedTransaction {
        TeamEntity.all()
            .limit(page).offset(size.toLong())
            .orderBy(TeamsTable.updatedAt to SortOrder.ASC)
            .map { TeamMapper.toDomain(it) }
    }

    override suspend fun existsByName(name: String): Boolean = newSuspendedTransaction {
        !TeamEntity.find {
            (TeamsTable.name eq name)
        }.empty()
    }

    override suspend fun create(team: Team): Team = newSuspendedTransaction {
        TeamMapper.toDomain(TeamMapper.toEntity(team))
    }

    override suspend fun update(team: Team): Team = newSuspendedTransaction {
        val entity = TeamEntity.findById(team.id)
            ?: throw IllegalArgumentException("Team not found with id: ${team.id}")
        TeamMapper.updateEntity(entity, team)
        TeamMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        TeamEntity.findById(id)?.delete() != null
    }
}