package com.quadro.project.infrastructure.database.repositories

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.repositories.ProjectRepository
import com.quadro.project.infrastructure.database.entities.ProjectEntity
import com.quadro.project.infrastructure.database.entities.ProjectMembersTable
import com.quadro.project.infrastructure.database.entities.ProjectsTable
import com.quadro.project.infrastructure.database.mappers.ProjectMapper
import com.quadro.shared.utils.toOffsetDateTime
import io.ktor.util.debug.addToContextInDebugMode
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Clock

class ProjectRepositoryImpl : ProjectRepository {
    override suspend fun create(project: Project): Project = newSuspendedTransaction {
        ProjectMapper.toDomain(ProjectMapper.toEntity(project))
    }

    override suspend fun findById(id: UUID): Project? = newSuspendedTransaction {
        ProjectEntity.findById(id)?.let { ProjectMapper.toDomain(it) }
    }

    override suspend fun findByKey(key: String): Project? = newSuspendedTransaction {
        ProjectEntity.find { ProjectsTable.key eq key.uppercase() }
            .firstOrNull()?.let { ProjectMapper.toDomain(it) }
    }

    override suspend fun findByName(name: String): Project? = newSuspendedTransaction {
        ProjectEntity.find { ProjectsTable.name eq name }
            .firstOrNull()?.let { ProjectMapper.toDomain(it) }
    }

    override suspend fun update(project: Project): Project = newSuspendedTransaction {
        val entity = ProjectEntity.findById(project.id)
            ?: throw IllegalArgumentException("Project not found with id: ${'$'}{project.id}")

        ProjectMapper.updateEntity(entity, project)
        ProjectMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        ProjectEntity.findById(id)?.delete() != null
    }

    override suspend fun findByUser(
        userId: UUID,
        limit: Int,
        offset: Int
    ): List<Project> = newSuspendedTransaction {
        val query = ProjectsTable.join(
            ProjectMembersTable,
            JoinType.INNER,
            additionalConstraint = { ProjectsTable.id eq ProjectMembersTable.projectId }
        ).selectAll()
            .where { ProjectMembersTable.userId eq userId }

        query
            .orderBy(ProjectsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { ProjectMapper.toDomain(ProjectEntity.wrapRow(it)) }
    }

    override suspend fun existsByName(name: String): Boolean = newSuspendedTransaction {
        !ProjectEntity.find { ProjectsTable.name eq name}.empty()
    }

    override suspend fun existsByKey(key: String): Boolean = newSuspendedTransaction {
        !ProjectEntity.find { ProjectsTable.key eq key}.empty()
    }

    override suspend fun updateStatus(
        id: UUID,
        status: ProjectStatus
    ): Boolean = newSuspendedTransaction {
        ProjectEntity.findById(id)?.apply {
            this.status = status.name
            updatedAt = Clock.System.now().toOffsetDateTime()
        } != null
    }
}