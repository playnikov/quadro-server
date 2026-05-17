package com.quadro.project.infrastructure.database.repositories

import com.quadro.project.domain.models.InvitationStatus
import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.ProjectInvitation
import com.quadro.project.domain.repositories.ProjectInvitationRepository
import com.quadro.project.infrastructure.database.entities.ProjectInvitationEntity
import com.quadro.project.infrastructure.database.entities.ProjectInvitationsTable
import com.quadro.project.infrastructure.database.mappers.ProjectInvitationMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Clock

class ProjectInvitationRepositoryImpl : ProjectInvitationRepository {
    override suspend fun create(invitation: ProjectInvitation): ProjectInvitation = newSuspendedTransaction {
        val entity = ProjectInvitationMapper.toEntity(invitation)
        ProjectInvitationMapper.toDomain(entity)
    }

    override suspend fun findById(id: UUID): ProjectInvitation? = newSuspendedTransaction {
        ProjectInvitationEntity.findById(id)?.let { ProjectInvitationMapper.toDomain(it) }
    }

    override suspend fun findByToken(token: String): ProjectInvitation? = newSuspendedTransaction {
        ProjectInvitationEntity.find { ProjectInvitationsTable.token eq token }
            .firstOrNull()
            ?.let { ProjectInvitationMapper.toDomain(it) }
    }

    override suspend fun findByProject(
        projectId: UUID,
        status: InvitationStatus?
    ): List<ProjectInvitation> = newSuspendedTransaction {
        val query = ProjectInvitationEntity.find { ProjectInvitationsTable.projectId eq projectId }
        val filtered = if (status != null) {
            query.filter { it.status == status.name }
        } else query
        filtered.map { ProjectInvitationMapper.toDomain(it) }
    }

    override suspend fun findByEmail(email: String): List<ProjectInvitation> = newSuspendedTransaction {
        ProjectInvitationEntity.find {
            (ProjectInvitationsTable.inviteType eq InviteType.EMAIL.name) and
                    (ProjectInvitationsTable.identifier eq email)
        }.map(ProjectInvitationMapper::toDomain)
    }

    override suspend fun updateStatus(
        id: UUID,
        status: InvitationStatus
    ): Boolean = newSuspendedTransaction {
        ProjectInvitationEntity.findById(id)?.apply {
            this.status = status.name
        } != null
    }

    override suspend fun acceptInvitation(id: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        ProjectInvitationEntity.findById(id)?.apply {
            this.status = InvitationStatus.ACCEPTED.name
            this.acceptedAt = Clock.System.now().toOffsetDateTime()
            this.acceptedBy = userId
        } != null
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        ProjectInvitationEntity.findById(id)?.delete() != null
    }

    override suspend fun deleteExpired(): Int = newSuspendedTransaction {
        val now = Clock.System.now().toOffsetDateTime()
        val expired = ProjectInvitationEntity.find {
            (ProjectInvitationsTable.expiresAt lessEq now) and
                    (ProjectInvitationsTable.status eq InvitationStatus.PENDING.name)
        }.toList()

        expired.forEach { it.status = InvitationStatus.EXPIRED.name }
        expired.size
    }

    override suspend fun countPendingByProject(projectId: UUID): Long = newSuspendedTransaction {
        ProjectInvitationEntity.find {
            (ProjectInvitationsTable.projectId eq projectId) and
                    (ProjectInvitationsTable.status eq InvitationStatus.PENDING.name)
        }.count()
    }
}