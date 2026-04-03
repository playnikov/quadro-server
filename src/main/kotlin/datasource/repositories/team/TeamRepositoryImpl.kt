package com.quadro.datasource.repositories.team

import com.quadro.datasource.entities.ProjectMembersTable
import com.quadro.datasource.entities.ProjectsTable
import com.quadro.datasource.entities.TeamEntity
import com.quadro.datasource.entities.TeamMembersTable
import com.quadro.datasource.entities.TeamStatusDb
import com.quadro.datasource.entities.TeamVisibilityDb
import com.quadro.datasource.entities.TeamsTable
import com.quadro.datasource.mappers.TeamMapper
import com.quadro.domain.models.team.Team
import com.quadro.domain.models.team.TeamStatus
import com.quadro.domain.models.team.TeamVisibility
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class TeamRepositoryImpl : TeamRepository {
    override suspend fun create(team: Team): Team = newSuspendedTransaction {
        TeamMapper.toDomain(TeamMapper.toEntity(team))
    }

    override suspend fun findById(id: UUID): Team? = newSuspendedTransaction {
        TeamEntity.findById(id)?.let { TeamMapper.toDomain(it) }
    }

    override suspend fun findByName(companyId: UUID, name: String): Team? = newSuspendedTransaction {
        TeamEntity.find {
            (TeamsTable.companyId eq companyId) and
                    (TeamsTable.name eq name)
        }.firstOrNull()?.let { TeamMapper.toDomain(it) }
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

    override suspend fun findByCompany(
        companyId: UUID,
        limit: Int,
        offset: Int
    ): List<Team> = newSuspendedTransaction {
        TeamEntity.find { TeamsTable.companyId eq companyId }
            .orderBy(TeamsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { TeamMapper.toDomain(it) }
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?
    ): List<Team> = newSuspendedTransaction {
        fun addCompanyFilter(op: Op<Boolean>): Op<Boolean> {
            return companyId?.let {
                op and (ProjectsTable.companyId eq it)
            } ?: op
        }

        val query = TeamsTable
            .join(TeamMembersTable, JoinType.INNER,
                additionalConstraint = { TeamsTable.id eq TeamMembersTable.teamId })
            .selectAll()
            .where {
                addCompanyFilter(ProjectMembersTable.userId eq userId)
            }
            .withDistinct()

        val filteredQuery = companyId?.let {
            query.andWhere { TeamsTable.companyId eq companyId }
        } ?: query

        filteredQuery
            .orderBy(TeamsTable.createdAt to SortOrder.DESC)
            .map { TeamMapper.toDomain(TeamEntity.wrapRow(it)) }
    }

    override suspend fun countByCompany(companyId: UUID): Long = newSuspendedTransaction {
        TeamEntity.find { TeamsTable.companyId eq companyId }.count()
    }

    override suspend fun countByUser(userId: UUID, companyId: UUID?): Long = newSuspendedTransaction {
        fun addCompanyFilter(op: Op<Boolean>): Op<Boolean> {
            return companyId?.let {
                op and (ProjectsTable.companyId eq it)
            } ?: op
        }

        val query = TeamsTable
            .join(TeamMembersTable, JoinType.INNER,
                additionalConstraint = { TeamsTable.id eq TeamMembersTable.teamId })
            .selectAll()
            .where {
                addCompanyFilter(ProjectMembersTable.userId eq userId)
            }
            .withDistinct()

        val filteredQuery = companyId?.let {
            query.andWhere { TeamsTable.companyId eq companyId }
        } ?: query
        filteredQuery.count()
    }

    override suspend fun existsByName(companyId: UUID, name: String): Boolean = newSuspendedTransaction {
        !TeamEntity.find {
            (TeamsTable.companyId eq companyId) and
                    (TeamsTable.name eq name)
        }.empty()
    }

    override suspend fun updateStatus(id: UUID, status: TeamStatus): Boolean = newSuspendedTransaction {
        TeamEntity.findById(id)?.apply {
            this.status = when (status) {
                TeamStatus.ACTIVE -> TeamStatusDb.ACTIVE
                TeamStatus.ARCHIVED -> TeamStatusDb.ARCHIVED
                TeamStatus.DISBANDED -> TeamStatusDb.DISBANDED
            }
            if (status == TeamStatus.ARCHIVED) {
                archivedAt = Instant.now()
            }
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun updateVisibility(
        id: UUID,
        visibility: TeamVisibility
    ): Boolean = newSuspendedTransaction {
        TeamEntity.findById(id)?.apply {
            this.visibility = when (visibility) {
                TeamVisibility.PUBLIC -> TeamVisibilityDb.PUBLIC
                TeamVisibility.PRIVATE -> TeamVisibilityDb.PRIVATE
                TeamVisibility.HIDDEN -> TeamVisibilityDb.HIDDEN
            }
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun incrementMemberCount(id: UUID): Boolean = newSuspendedTransaction {
        TeamEntity.findById(id)?.apply {
            currentMembers += 1
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun decrementMemberCount(id: UUID): Boolean = newSuspendedTransaction {
        TeamEntity.findById(id)?.apply {
            currentMembers -= 1
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun changeLead(id: UUID, newLeadId: UUID): Boolean = newSuspendedTransaction {
        TeamEntity.findById(id)?.apply {
            leadId = newLeadId
            updatedAt = Instant.now()
        } != null
    }

    override suspend fun search(
        companyId: UUID,
        query: String,
        limit: Int
    ): List<Team> = newSuspendedTransaction {
        val searchPattern = "%${query.lowercase()}%"
        TeamEntity.find {
            (TeamsTable.companyId eq companyId) and
                    (TeamsTable.name.lowerCase() like searchPattern or
                            (TeamsTable.description.lowerCase() like searchPattern))
        }
            .limit(limit)
            .map { TeamMapper.toDomain(it) }
    }
}