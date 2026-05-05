package com.quadro.task.infrastructure.database.repositories.project

import com.quadro.task.domain.models.project.Project
import com.quadro.task.domain.repositories.project.ProjectRepository
import com.quadro.task.infrastructure.database.entities.project.ProjectEntity
import com.quadro.task.infrastructure.database.entities.project.ProjectsTable
import com.quadro.task.infrastructure.database.mappers.project.ProjectMapper
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ProjectRepositoryImpl : ProjectRepository {
    override suspend fun upsert(project: Project): Unit = newSuspendedTransaction {
        val existing = ProjectEntity.findById(project.id)
        if (existing != null) {
            ProjectMapper.updateEntity(existing, project)
        } else {
            ProjectMapper.newEntity(project)
        }
    }

    override suspend fun findById(id: UUID): Project? = newSuspendedTransaction {
        ProjectEntity.findById(id)?.let(ProjectMapper::toDomain)
    }

    override suspend fun findByKey(key: String): Project? = newSuspendedTransaction {
        ProjectEntity.find { ProjectsTable.key eq key }
            .firstOrNull()?.let(ProjectMapper::toDomain)
    }

    override suspend fun findAll(): List<Project> = newSuspendedTransaction {
        ProjectEntity.all().map(ProjectMapper::toDomain)
    }

    override suspend fun delete(id: UUID): Unit = newSuspendedTransaction {
        ProjectEntity.findById(id)?.delete()
    }
}