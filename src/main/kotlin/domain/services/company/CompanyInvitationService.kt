package com.quadro.domain.services.company

import com.quadro.domain.models.company.CompanyResult
import com.quadro.domain.models.company.InvitationCreate
import com.quadro.domain.models.company.InvitationResult
import java.util.UUID

interface CompanyInvitationService {
    suspend fun createInvitation(companyId: UUID, userId: UUID, request: InvitationCreate): Result<InvitationResult>
    suspend fun acceptInvitation(userId: UUID, token: String): Result<CompanyResult>
    suspend fun getInvitations(companyId: UUID, userId: UUID): Result<List<InvitationResult>>
    suspend fun cancelInvitation(companyId: UUID, userId: UUID, invitationId: UUID): Result<Unit>
    suspend fun resendInvitation(companyId: UUID, userId: UUID, invitationId: UUID): Result<InvitationResult>
}