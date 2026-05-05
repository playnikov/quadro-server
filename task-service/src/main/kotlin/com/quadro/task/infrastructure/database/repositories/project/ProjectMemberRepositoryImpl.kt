package com.quadro.task.infrastructure.database.repositories.project

import com.quadro.task.domain.models.project.ProjectMember
import com.quadro.task.domain.repositories.project.ProjectMemberRepository
import com.quadro.task.infrastructure.database.entities.project.ProjectMemberEntity
import com.quadro.task.infrastructure.database.entities.project.ProjectMembersTable
import com.quadro.task.infrastructure.database.mappers.project.ProjectMemberMapper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class ProjectMemberRepositoryImpl : ProjectMemberRepository {
    override suspend fun upsert(member: ProjectMember): Unit = newSuspendedTransaction {
        val existing = ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq member.projectId) and
                    (ProjectMembersTable.userId eq member.userId)
        }.firstOrNull()
        if (existing != null) {
            ProjectMemberMapper.updateEntity(existing, member)
        } else {
            ProjectMemberMapper.newEntity(member)
        }
    }

    override suspend fun findByProjectAndUser(
        projectId: UUID,
        userId: UUID
    ): ProjectMember? = newSuspendedTransaction {
        ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq projectId) and
                    (ProjectMembersTable.userId eq userId)
        }.firstOrNull()?.let(ProjectMemberMapper::toDomain)
    }

    override suspend fun findByProjectId(projectId: UUID): List<ProjectMember> = newSuspendedTransaction {
        ProjectMemberEntity.find { ProjectMembersTable.projectId eq projectId }
            .map { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun findByUserId(userId: UUID): List<ProjectMember> = newSuspendedTransaction {
        ProjectMemberEntity.find { ProjectMembersTable.userId eq userId }
            .map { ProjectMemberMapper.toDomain(it) }
    }

    override suspend fun delete(projectId: UUID, userId: UUID): Unit = newSuspendedTransaction {
        ProjectMemberEntity.find {
            (ProjectMembersTable.projectId eq projectId) and
                    (ProjectMembersTable.userId eq userId)
        }.firstOrNull()?.delete()
    }

    override suspend fun deleteByUserId(userId: UUID): Unit = newSuspendedTransaction {
        ProjectMemberEntity.find {
            ProjectMembersTable.userId eq userId
        }.forEach { it.delete() }
    }

    override suspend fun deleteByProject(projectId: UUID): Unit = newSuspendedTransaction {
        ProjectMemberEntity.find {
            ProjectMembersTable.projectId eq projectId
        }.forEach { it.delete() }
    }
}