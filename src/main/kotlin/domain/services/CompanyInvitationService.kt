package com.quadro.domain.services

import com.quadro.domain.models.AcceptInvitation
import com.quadro.domain.models.CompanyResult
import com.quadro.domain.models.CompanyRole
import com.quadro.domain.models.InvitationCreate
import com.quadro.domain.models.InvitationResult
import java.util.UUID

interface CompanyInvitationService {
    suspend fun createInvitation(companyId: UUID, userId: UUID, request: InvitationCreate): Result<InvitationResult>
    suspend fun acceptInvitation(userId: UUID, request: AcceptInvitation): Result<CompanyResult>
    suspend fun getInvitations(companyId: UUID, userId: UUID): Result<List<InvitationResult>>
    suspend fun cancelInvitation(companyId: UUID, userId: UUID, invitationId: UUID): Result<Unit>
    suspend fun resendInvitation(companyId: UUID, userId: UUID, invitationId: UUID): Result<InvitationResult>
    suspend fun generateInviteLink(companyId: UUID, userId: UUID, role: CompanyRole): Result<String>
}