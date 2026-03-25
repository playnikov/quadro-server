package com.quadro.company.domain.services

import com.quadro.company.domain.models.Company
import com.quadro.company.domain.models.CompanyInvitation
import com.quadro.company.domain.models.CompanyResponse
import com.quadro.company.domain.models.InvitationCreate
import com.quadro.company.domain.models.InvitationResponse
import java.util.UUID

interface CompanyInvitationService {
    suspend fun createInvitation(companyId: UUID, userId: UUID, request: InvitationCreate): Result<InvitationResponse>
    suspend fun acceptInvitation(token: String, userId: UUID): Result<CompanyResponse>
    suspend fun getInvitations(companyId: UUID, userId: UUID): Result<List<InvitationResponse>>
    suspend fun cancelInvitation(companyId: UUID, userId: UUID, invitationId: UUID): Result<Unit>
}