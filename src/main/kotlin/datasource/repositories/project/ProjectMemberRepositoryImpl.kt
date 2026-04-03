package com.quadro.datasource.repositories.project

import com.quadro.datasource.entities.ProjectEntity
import com.quadro.datasource.entities.ProjectMemberEntity
import com.quadro.datasource.entities.ProjectMembersTable
import com.quadro.datasource.mappers.ProjectMemberMapper
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectRole
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ProjectMemberRepositoryImpl : ProjectMemberRepository {
    override suspend fun add(member: ProjectMember): ProjectMember = newSuspendedTransaction {
        ProjectMemberMapper.toDomain(ProjectMemberMapper.toEntity(member))
    }

    override suspend fun addAll(members: List<ProjectMember>): List<ProjectMember> = newSuspendedTransaction {
        members.map { member ->
            ProjectMemberMapper.toDomain(ProjectMemberMapper.toEntity(member))
        }
    }

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
            .orderBy(ProjectMembersTable.joinedAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset.toLong())
            .map { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?
    ): List<ProjectMember> = newSuspendedTransaction {
        val query = ProjectMemberEntity.find { ProjectMembersTable.userId eq userId }

        if (companyId != null) {
            query.filter { member ->
                ProjectEntity.findById(member.projectId)?.companyId == companyId
            }
        } else {
            query.toList()
        }.map { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun findByTeam(
        teamId: UUID,
        projectId: UUID?
    ): List<ProjectMember> = newSuspendedTransaction {
        val query = ProjectMemberEntity.find { ProjectMembersTable.sourceTeamId eq teamId }

        if (projectId != null) {
            query.filter { it.projectId == projectId }
        } else {
            query.toList()
        }.map { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun updateRole(
        id: UUID,
        role: ProjectRole
    ): Boolean = newSuspendedTransaction {
        ProjectMemberEntity.findById(id)?.apply {
            this.role = role.name
        } != null
    }

    override suspend fun updateLastActive(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun remove(id: UUID): Boolean =  newSuspendedTransaction {
        ProjectMemberEntity.findById(id)?.delete() != null
    }

    override suspend fun removeByProjectAndUser(projectId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        val member = findByProjectAndUser(projectId, userId)
        if (member != null) {
            ProjectMemberEntity.findById(member.id)?.delete() != null
        } else false
    }

    override suspend fun removeAllByProject(projectId: UUID): Int = newSuspendedTransaction {
        val members = ProjectMemberEntity.find { ProjectMembersTable.projectId eq projectId }.toList()
        members.forEach { it.delete() }
        members.size
    }

    override suspend fun removeAllByTeam(teamId: UUID, projectId: UUID?): Int = newSuspendedTransaction {
        val members = ProjectMemberEntity.find { ProjectMembersTable.sourceTeamId eq teamId }
            .let { query ->
                if (projectId != null) {
                    query.filter { it.projectId == projectId }
                } else {
                    query.toList()
                }
            }
        members.forEach { it.delete() }
        members.size
    }

    override suspend fun countByProject(projectId: UUID): Long = newSuspendedTransaction {
        ProjectMemberEntity.find { ProjectMembersTable.projectId eq projectId }.count()
    }

    override suspend fun countByUser(userId: UUID, companyId: UUID?): Long  = newSuspendedTransaction {
        val members = ProjectMemberEntity.find { ProjectMembersTable.userId eq userId }

        if (companyId != null) {
            members.count { member ->
                ProjectEntity.findById(member.projectId)?.companyId == companyId
            }.toLong()
        } else {
            members.count()
        }
    }

    override suspend fun exists(projectId: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        !ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq projectId) and
                    (ProjectMembersTable.userId eq userId)
        }.empty()
    }

    override suspend fun syncTeamMembers(
        projectId: UUID,
        teamId: UUID,
        role: ProjectRole
    ): Pair<Int, Int> {
        TODO("Not yet implemented")
    }
}