package com.quadro.company.infrastructure.database.repositories

import com.quadro.company.domain.models.CompanyInvitation
import com.quadro.company.domain.models.InvitationStatus
import com.quadro.company.domain.repositories.CompanyInvitationRepository
import com.quadro.company.infrastructure.database.entities.CompanyInvitationEntity
import com.quadro.company.infrastructure.database.entities.CompanyInvitationsTable
import com.quadro.company.infrastructure.database.mappers.CompanyInvitationMapper
import com.quadro.shared.utils.toOffsetDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.time.Clock

class CompanyInvitationRepositoryImpl : CompanyInvitationRepository {
    override suspend fun create(invitation: CompanyInvitation): CompanyInvitation = newSuspendedTransaction {
        val entity = CompanyInvitationMapper.toEntity(invitation)
        CompanyInvitationMapper.toDomain(entity)
    }

    override suspend fun findById(id: UUID): CompanyInvitation? = newSuspendedTransaction {
        CompanyInvitationEntity.findById(id)?.let { CompanyInvitationMapper.toDomain(it) }
    }

    override suspend fun findByToken(token: String): CompanyInvitation? = newSuspendedTransaction {
        CompanyInvitationEntity.find { CompanyInvitationsTable.token eq token }
            .firstOrNull()
            ?.let { CompanyInvitationMapper.toDomain(it) }
    }

    override suspend fun findByCompany(
        companyId: UUID,
        status: InvitationStatus?
    ): List<CompanyInvitation> = newSuspendedTransaction {
        val query = CompanyInvitationEntity.find { CompanyInvitationsTable.companyId eq companyId }
        val filtered = if (status != null) {
            query.filter { it.status == status.name }
        } else query
        filtered.map { CompanyInvitationMapper.toDomain(it) }
    }

    override suspend fun updateStatus(
        id: UUID,
        status: InvitationStatus
    ): Boolean = newSuspendedTransaction {
        CompanyInvitationEntity.findById(id)?.apply {
            this.status = status.name
        } != null
    }

    override suspend fun acceptInvitation(id: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        CompanyInvitationEntity.findById(id)?.apply {
            this.status = InvitationStatus.ACCEPTED.name
            this.acceptedAt = Clock.System.now().toOffsetDateTime()
            this.acceptedBy = userId
        } != null
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        CompanyInvitationEntity.findById(id)?.delete() != null
    }

    override suspend fun deleteExpired(): Int = newSuspendedTransaction {
        val now = Clock.System.now().toOffsetDateTime()
        val expired = CompanyInvitationEntity.find {
            (CompanyInvitationsTable.expiresAt lessEq now) and
                    (CompanyInvitationsTable.status eq InvitationStatus.PENDING.name)
        }.toList()

        expired.forEach { it.status = InvitationStatus.EXPIRED.name }
        expired.size
    }

    override suspend fun countPendingByCompany(companyId: UUID): Long = newSuspendedTransaction {
        CompanyInvitationEntity.find {
            (CompanyInvitationsTable.companyId eq companyId) and
                    (CompanyInvitationsTable.status eq InvitationStatus.PENDING.name)
        }.count()
    }
}