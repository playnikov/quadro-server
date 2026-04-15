package com.quadro.team.infrastructure.database.repositories

import com.quadro.team.domain.models.Company
import com.quadro.team.domain.models.Project
import com.quadro.team.domain.repositories.CompanyRepository
import com.quadro.team.domain.repositories.ProjectRepository
import com.quadro.team.infrastructure.database.entities.CompanyEntity
import com.quadro.team.infrastructure.database.entities.ProjectEntity
import com.quadro.team.infrastructure.database.mappers.CompanyMapper
import com.quadro.team.infrastructure.database.mappers.ProjectMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ProjectRepositoryImpl : ProjectRepository {
    override suspend fun upsert(project: Project): Project = newSuspendedTransaction {
        val existing = ProjectEntity.findById(project.id)
        val entity = if (existing != null) {
            ProjectMapper.updateEntity(existing, project)
            existing
        } else {
            ProjectMapper.newEntity(project)
        }
        ProjectMapper.toDomain(entity)
    }

    override suspend fun findById(id: UUID): Project? = newSuspendedTransaction {
        ProjectEntity.findById(id)?.let { ProjectMapper.toDomain(it) }
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        ProjectEntity.findById(id)?.delete() != null
    }

}