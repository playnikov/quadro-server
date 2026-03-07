package com.quadro.datasource.repositories.company

import com.quadro.datasource.entities.CompanyInvitationEntity
import com.quadro.datasource.entities.CompanyInvitationsTable
import com.quadro.datasource.mappers.CompanyInvitationMapper
import com.quadro.domain.models.company.CompanyInvitation
import com.quadro.domain.models.company.InvitationStatus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

class CompanyInvitationRepositoryImpl : CompanyInvitationRepository {
    override suspend fun create(invitation: CompanyInvitation): CompanyInvitation = newSuspendedTransaction {
        val entity = CompanyInvitationMapper.toEntity(invitation)
        CompanyInvitationMapper.toDomain(entity)
    }

    override suspend fun update(invitation: CompanyInvitation): CompanyInvitation = newSuspendedTransaction {
        val entity = CompanyInvitationEntity.findById(invitation.id)
            ?: throw IllegalArgumentException("Company invitation not found with id: ${invitation.id}")
        CompanyInvitationMapper.updateEntity(entity, invitation)
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
            query.filter { it.status == status }
        } else query
        filtered.map { CompanyInvitationMapper.toDomain(it) }
    }

    override suspend fun updateStatus(
        id: UUID,
        status: InvitationStatus
    ): Boolean = newSuspendedTransaction {
        CompanyInvitationEntity.findById(id)?.apply {
            this.status = status
        } != null
    }

    override suspend fun acceptInvitation(id: UUID, userId: UUID): Boolean = newSuspendedTransaction {
        CompanyInvitationEntity.findById(id)?.apply {
            this.status = InvitationStatus.ACCEPTED
            this.acceptedAt = Instant.now()
            this.acceptedBy = userId
        } != null
    }

    override suspend fun delete(id: UUID): Boolean = newSuspendedTransaction {
        CompanyInvitationEntity.findById(id)?.delete() != null
    }

    override suspend fun deleteExpired(): Int = newSuspendedTransaction {
        val now = Instant.now()
        val expired = CompanyInvitationEntity.find {
            (CompanyInvitationsTable.expiresAt lessEq now) and
                    (CompanyInvitationsTable.status eq InvitationStatus.PENDING)
        }.toList()

        expired.forEach { it.status = InvitationStatus.EXPIRED }
        expired.size
    }

    override suspend fun countPendingByCompany(companyId: UUID): Long = newSuspendedTransaction {
        CompanyInvitationEntity.find {
            (CompanyInvitationsTable.companyId eq companyId) and
                    (CompanyInvitationsTable.status eq InvitationStatus.PENDING)
        }.count()
    }
}