package com.quadro.datasource.repositories.project

import com.quadro.datasource.entities.ProjectEntity
import com.quadro.datasource.entities.ProjectMembersTable
import com.quadro.datasource.entities.ProjectTeamsTable
import com.quadro.datasource.entities.ProjectsTable
import com.quadro.datasource.entities.TeamMembersTable
import com.quadro.datasource.mappers.ProjectMapper
import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectStatus
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class ProjectRepositoryImpl : ProjectRepository {
    override suspend fun create(project: Project): Project = newSuspendedTransaction{
        ProjectMapper.toDomain(ProjectMapper.toEntity(project))
    }

    override suspend fun findById(id: UUID): Project? = newSuspendedTransaction {
        ProjectEntity.findById(id)?.let { ProjectMapper.toDomain(it) }
    }

    override suspend fun findByKey(
        companyId: UUID,
        key: String
    ): Project? = newSuspendedTransaction {
        ProjectEntity.find {
            (ProjectsTable.companyId eq companyId) and
                    (ProjectsTable.key eq key.uppercase())
        }.firstOrNull()?.let { ProjectMapper.toDomain(it) }
    }

    override suspend fun findByName(
        companyId: UUID,
        name: String
    ): Project? = newSuspendedTransaction {
        ProjectEntity.find {
            (ProjectsTable.companyId eq companyId) and
                    (ProjectsTable.name eq name)
        }.firstOrNull()?.let { ProjectMapper.toDomain(it) }
    }

    override suspend fun update(project: Project): Project = newSuspendedTransaction {
        val entity = ProjectEntity.findById(project.id)
            ?: throw IllegalArgumentException("Project not found with id: ${project.id}")

        ProjectMapper.updateEntity(entity, project)
        ProjectMapper.toDomain(entity)
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        ProjectEntity.findById(id)?.delete() != null
    }

    override suspend fun findByCompany(
        companyId: UUID,
        limit: Int,
        offset: Int
    ): List<Project> = newSuspendedTransaction {
        ProjectEntity.find { ProjectsTable.companyId eq companyId }
            .orderBy(ProjectsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { ProjectMapper.toDomain(it) }
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?
    ): List<Project> = newSuspendedTransaction {
        fun addCompanyFilter(op: Op<Boolean>): Op<Boolean> {
            return companyId?.let {
                op and (ProjectsTable.companyId eq it)
            } ?: op
        }

        val directQuery = ProjectsTable
            .join(ProjectMembersTable, JoinType.INNER,
                additionalConstraint = { ProjectsTable.id eq ProjectMembersTable.projectId }
            )
            .selectAll()
            .where {
                addCompanyFilter(ProjectMembersTable.userId eq userId)
            }
            .withDistinct()

        val teamQuery = ProjectsTable
            .join(ProjectTeamsTable, JoinType.INNER,
                additionalConstraint = { ProjectsTable.id eq ProjectTeamsTable.projectId })
            .join(TeamMembersTable, JoinType.INNER,
                additionalConstraint = { ProjectTeamsTable.teamId eq TeamMembersTable.teamId })
            .selectAll()
            .where {
                addCompanyFilter(TeamMembersTable.userId eq userId)
            }
            .withDistinct()

        val directResults = directQuery.map { ProjectMapper.toDomain(ProjectEntity.wrapRow(it)) }
        val teamResults = teamQuery.map { ProjectMapper.toDomain(ProjectEntity.wrapRow(it)) }

        (directResults + teamResults)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun findByTeam(teamId: UUID): List<Project> = newSuspendedTransaction {
        ProjectEntity.wrapRows(
            ProjectsTable.join(ProjectTeamsTable, JoinType.INNER,
                additionalConstraint = { ProjectsTable.id eq ProjectTeamsTable.projectId })
                .selectAll()
                .where { ProjectTeamsTable.teamId eq teamId }
                .orderBy(ProjectTeamsTable.teamId eq teamId)
        ).toList().map { ProjectMapper.toDomain(it) }
    }

    override suspend fun countByCompany(companyId: UUID): Long = newSuspendedTransaction {
        ProjectEntity.find { ProjectsTable.companyId eq companyId }.count()
    }

    override suspend fun countByUser(userId: UUID, companyId: UUID?): Long = newSuspendedTransaction {
        fun addCompanyFilter(op: Op<Boolean>): Op<Boolean> {
            return companyId?.let {
                op and (ProjectsTable.companyId eq it)
            } ?: op
        }

        val directQuery = ProjectsTable
            .join(ProjectMembersTable, JoinType.INNER,
                additionalConstraint = { ProjectsTable.id eq ProjectMembersTable.projectId }
            )
            .selectAll()
            .where {
                addCompanyFilter(ProjectMembersTable.userId eq userId)
            }
            .withDistinct()

        val teamQuery = ProjectsTable
            .join(ProjectTeamsTable, JoinType.INNER,
                additionalConstraint = { ProjectsTable.id eq ProjectTeamsTable.projectId })
            .join(TeamMembersTable, JoinType.INNER,
                additionalConstraint = { ProjectTeamsTable.teamId eq TeamMembersTable.teamId })
            .selectAll()
            .where {
                addCompanyFilter(TeamMembersTable.userId eq userId)
            }
            .withDistinct()

        val directResults = directQuery.map { ProjectMapper.toDomain(ProjectEntity.wrapRow(it)) }
        val teamResults = teamQuery.map { ProjectMapper.toDomain(ProjectEntity.wrapRow(it)) }

        (directResults + teamResults)
            .count().toLong()
    }

    override suspend fun existsByKey(companyId: UUID, key: String): Boolean = newSuspendedTransaction {
        !ProjectEntity.find {
            (ProjectsTable.companyId eq companyId) and
                    (ProjectsTable.key eq key.uppercase())
        }.empty()
    }

    override suspend fun existsByName(companyId: UUID, name: String): Boolean = newSuspendedTransaction {
        !ProjectEntity.find {
            (ProjectsTable.companyId eq companyId) and
                    (ProjectsTable.name eq name)
        }.empty()
    }

    override suspend fun updateStatus(
        id: UUID,
        status: ProjectStatus
    ): Boolean = newSuspendedTransaction {
        ProjectEntity.findById(id)?.apply {
            this.status = status.name
            if (status == ProjectStatus.COMPLETED) {
                completedAt = Instant.now()
            }
            if (status == ProjectStatus.ARCHIVED) {
                archivedAt = Instant.now()
            }
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun search(
        companyId: UUID,
        query: String,
        limit: Int
    ): List<Project> = newSuspendedTransaction {
        val searchPattern = "%${query.lowercase()}%"
        ProjectEntity.find {
            (ProjectsTable.companyId eq companyId) and
                    (ProjectsTable.name.lowerCase() like searchPattern or
                            (ProjectsTable.key.lowerCase() like searchPattern) or
                            (ProjectsTable.description.lowerCase() like searchPattern))
        }
            .limit(limit)
            .sortedByDescending { it.createdAt }
            .map { ProjectMapper.toDomain(it) }
    }
}