package com.quadro.project.infrastructure.database.repositories

import com.quadro.project.domain.models.InviteStatus
import com.quadro.project.domain.models.InviteType
import com.quadro.project.domain.models.ProjectInvitation
import com.quadro.project.domain.repositories.ProjectInvitationRepository
import com.quadro.project.infrastructure.database.entities.ProjectInvitationEntity
import com.quadro.project.infrastructure.database.entities.ProjectInvitationsTable
import com.quadro.project.infrastructure.database.mappers.ProjectInvitationMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
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
        val invitation = ProjectInvitationEntity.find { ProjectInvitationsTable.token eq token }
            .firstOrNull()
            ?.let { ProjectInvitationMapper.toDomain(it) }

        if (invitation != null &&
            invitation.status == InviteStatus.PENDING &&
            invitation.expiresAt < Clock.System.now()
        ) {
            ProjectInvitationsTable.update({ ProjectInvitationsTable.token eq token }) {
                it[ProjectInvitationsTable.status] = InviteStatus.EXPIRED
            }
            return@newSuspendedTransaction null
        }
        invitation
    }

    override suspend fun findByProject(projectId: UUID): List<ProjectInvitation> = newSuspendedTransaction {
        val invitations = ProjectInvitationEntity.find { ProjectInvitationsTable.projectId eq projectId }
            .map { ProjectInvitationMapper.toDomain(it) }

        invitations.map { expireIfNeeded(it) }
    }

    override suspend fun findByEmail(email: String): List<ProjectInvitation> = newSuspendedTransaction {
        val invitations = ProjectInvitationEntity.find {
            (ProjectInvitationsTable.type eq InviteType.EMAIL) and
                    (ProjectInvitationsTable.identifier eq email)
        }.map(ProjectInvitationMapper::toDomain)

        invitations.map { expireIfNeeded(it) }
    }

    override suspend fun updateStatus(
        id: UUID,
        status: InviteStatus
    ): Boolean = newSuspendedTransaction {
        ProjectInvitationEntity.findById(id)?.apply {
            this.status = status
        } != null
    }

    override suspend fun acceptInvitation(id: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        ProjectInvitationEntity.findById(id)?.apply {
            this.status = InviteStatus.ACCEPTED
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
                    (ProjectInvitationsTable.status eq InviteStatus.PENDING)
        }.toList()

        expired.forEach { it.status = InviteStatus.EXPIRED }
        expired.size
    }

    override suspend fun countPendingByProject(projectId: UUID): Long = newSuspendedTransaction {
        ProjectInvitationEntity.find {
            (ProjectInvitationsTable.projectId eq projectId) and
                    (ProjectInvitationsTable.status eq InviteStatus.PENDING)
        }.count()
    }

    private suspend fun expireIfNeeded(invitation: ProjectInvitation): ProjectInvitation {
        return if (invitation.status == InviteStatus.PENDING && invitation.expiresAt < Clock.System.now()) {
            newSuspendedTransaction {
                ProjectInvitationsTable.update({ ProjectInvitationsTable.id eq invitation.id }) {
                    it[status] = InviteStatus.EXPIRED
                }
            }
            invitation.copy(status = InviteStatus.EXPIRED)
        } else {
            invitation
        }
    }
}