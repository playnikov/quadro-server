package com.quadro.task.infrastructure.database.repositories.team

import com.quadro.task.domain.models.team.Team
import com.quadro.task.domain.repositories.team.TeamRepository
import com.quadro.task.infrastructure.database.entities.project.ProjectEntity
import com.quadro.task.infrastructure.database.entities.task.TaskEntity
import com.quadro.task.infrastructure.database.entities.team.TeamEntity
import com.quadro.task.infrastructure.database.mappers.project.ProjectMapper
import com.quadro.task.infrastructure.database.mappers.team.TeamMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class TeamRepositoryImpl : TeamRepository {
    override suspend fun upsert(team: Team): Unit = newSuspendedTransaction {
        val existing = TeamEntity.findById(team.id)
        if (existing != null) {
            TeamMapper.updateEntity(existing, team)
        } else {
            TeamMapper.toEntity(team)
        }
    }

    override suspend fun findById(id: UUID): Team? = newSuspendedTransaction {
        TeamEntity.findById(id)?.let(TeamMapper::toDomain)
    }

    override suspend fun delete(id: UUID): Unit = newSuspendedTransaction {
        TeamEntity.findById(id)?.delete()
    }
}