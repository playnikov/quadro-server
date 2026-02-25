package com.quadro.domain.services

import com.quadro.domain.models.AcceptInvitation
import com.quadro.domain.models.CompanyResponse
import com.quadro.domain.models.CompanyRole
import com.quadro.domain.models.InvitationCreate
import com.quadro.domain.models.InvitationResponse
import java.util.UUID

interface CompanyInvitationService {
    suspend fun createInvitation(companyId: UUID, userId: UUID, request: InvitationCreate): Result<InvitationResponse>
    suspend fun acceptInvitation(userId: UUID, request: AcceptInvitation): Result<CompanyResponse>
    suspend fun getInvitations(companyId: UUID, userId: UUID): Result<List<InvitationResponse>>
    suspend fun cancelInvitation(companyId: UUID, userId: UUID, invitationId: UUID): Result<Unit>
    suspend fun resendInvitation(companyId: UUID, userId: UUID, invitationId: UUID): Result<InvitationResponse>
    suspend fun generateInviteLink(companyId: UUID, userId: UUID, role: CompanyRole): Result<String>
}