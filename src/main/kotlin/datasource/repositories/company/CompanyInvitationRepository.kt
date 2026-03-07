package com.quadro.datasource.repositories.company

import com.quadro.domain.models.company.CompanyInvitation
import com.quadro.domain.models.company.InvitationStatus
import java.util.UUID

interface CompanyInvitationRepository {
    suspend fun create(invitation: CompanyInvitation): CompanyInvitation
    suspend fun update(invitation: CompanyInvitation): CompanyInvitation
    suspend fun findById(id: UUID): CompanyInvitation?
    suspend fun findByToken(token: String): CompanyInvitation?
    suspend fun findByCompany(companyId: UUID, status: InvitationStatus?): List<CompanyInvitation>
    suspend fun updateStatus(id: UUID, status: InvitationStatus): Boolean
    suspend fun acceptInvitation(id: UUID, userId: UUID): Boolean
    suspend fun delete(id: UUID): Boolean
    suspend fun deleteExpired(): Int
    suspend fun countPendingByCompany(companyId: UUID): Long
}