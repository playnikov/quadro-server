package com.quadro.company.domain.repositories

import com.quadro.company.domain.models.CompanyInvitation
import com.quadro.company.domain.models.InvitationStatus
import java.util.UUID

interface CompanyInvitationRepository {
    suspend fun create(invitation: CompanyInvitation): CompanyInvitation
    suspend fun findById(id: UUID): CompanyInvitation?
    suspend fun findByToken(token: String): CompanyInvitation?
    suspend fun findByCompany(companyId: UUID, status: InvitationStatus?): List<CompanyInvitation>

    suspend fun updateStatus(id: UUID, status: InvitationStatus): Boolean
    suspend fun acceptInvitation(id: UUID, userId: UUID): Boolean
    suspend fun delete(id: UUID): Boolean
    suspend fun deleteExpired(): Int

    suspend fun countPendingByCompany(companyId: UUID): Long
}