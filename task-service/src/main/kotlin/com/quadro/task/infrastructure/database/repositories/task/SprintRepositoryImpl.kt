package com.quadro.task.infrastructure.database.repositories.task

import com.quadro.task.domain.models.task.Sprint
import com.quadro.task.domain.repositories.task.SprintRepository
import com.quadro.task.infrastructure.database.entities.task.SprintEntity
import com.quadro.task.infrastructure.database.entities.task.SprintsTable
import com.quadro.task.infrastructure.database.mappers.task.SprintMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class SprintRepositoryImpl : SprintRepository {
    override suspend fun create(sprint: Sprint): Sprint = newSuspendedTransaction {
        val entity = SprintMapper.toEntity(sprint)
        SprintMapper.toDomain(entity)
    }

    override suspend fun findById(id: UUID): Sprint? = newSuspendedTransaction {
        SprintEntity.findById(id)?.let(SprintMapper::toDomain)
    }

    override suspend fun findByProjectId(projectId: UUID): List<Sprint> = newSuspendedTransaction {
        SprintEntity.find { SprintsTable.projectId eq projectId }
            .map(SprintMapper::toDomain)
    }

    override suspend fun update(sprint: Sprint): Sprint = newSuspendedTransaction {
        val entity = SprintEntity.findById(sprint.id)
            ?: throw IllegalArgumentException("Sprint not found with id: ${sprint.id}")
        SprintMapper.updateEntity(entity, sprint)
        SprintMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Unit = newSuspendedTransaction {
        SprintEntity.findById(id)?.delete()
    }
}