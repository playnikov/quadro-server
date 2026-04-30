package com.quadro.project.infrastructure.database.repositories

import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.ProjectRole
import com.quadro.project.domain.repositories.ProjectMemberRepository
import com.quadro.project.infrastructure.database.entities.ProjectMemberEntity
import com.quadro.project.infrastructure.database.entities.ProjectMembersTable
import com.quadro.project.infrastructure.database.mappers.ProjectMemberMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant

class ProjectMemberRepositoryImpl : ProjectMemberRepository {
    override suspend fun findById(id: UUID): ProjectMember? = newSuspendedTransaction {
        ProjectMemberEntity.findById(id)?.let { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun findByProjectAndUser(
        projectId: UUID,
        userId: UUID
    ): ProjectMember? = newSuspendedTransaction {
        ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq projectId) and
                    (ProjectMembersTable.userId eq userId)
        }.firstOrNull()?.let { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun findByProject(
        projectId: UUID,
        limit: Int,
        offset: Int
    ): List<ProjectMember> = newSuspendedTransaction {
        ProjectMemberEntity.find { ProjectMembersTable.projectId eq projectId }
            .limit(limit).offset(offset.toLong())
            .map { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun findByProjectAndRole(
        projectId: UUID,
        role: ProjectRole
    ): List<ProjectMember> = newSuspendedTransaction {
        ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq projectId) and
                    (ProjectMembersTable.role eq role.name)
        }.map { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun exists(projectId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq projectId) and
                    (ProjectMembersTable.userId eq userId)
        }.count() > 0
    }

    override suspend fun add(member: ProjectMember): ProjectMember = newSuspendedTransaction {
        val entity = ProjectMemberMapper.toEntity(member)
        ProjectMemberMapper.toDomain(entity)
    }

    override suspend fun remove(id: UUID): Unit = newSuspendedTransaction {
        ProjectMemberEntity.findById(id)?.delete()
    }

    override suspend fun updateRole(id: UUID, role: ProjectRole): Unit = newSuspendedTransaction {
        ProjectMemberEntity.findById(id)?.apply {
            this.role = role.name
        } != null
    }

    override suspend fun countByProject(projectId: UUID): Long = newSuspendedTransaction {
        ProjectMembersTable.selectAll()
            .where { ProjectMembersTable.projectId eq projectId }
            .count()
    }

    override suspend fun findNewMembers(
        projectId: UUID,
        since: Instant
    ): List<ProjectMember> = newSuspendedTransaction {
        ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq projectId) and
                    (ProjectMembersTable.joinedAt greaterEq since.toOffsetDateTime())
        }.map { ProjectMemberMapper.toDomain(it) }
    }
}