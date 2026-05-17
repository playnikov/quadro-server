package com.quadro.project.domain.services

import com.quadro.project.domain.models.InvitationCreate
import com.quadro.project.domain.models.InvitationResponse
import com.quadro.project.presentation.models.ProjectResponse
import java.util.UUID

interface ProjectInvitationService {
    suspend fun createInvitation(projectId: UUID, userId: UUID, request: InvitationCreate): InvitationResponse
    suspend fun acceptInvitation(token: String, userId: UUID): ProjectResponse
    suspend fun getInvitations(projectId: UUID, userId: UUID): List<InvitationResponse>
    suspend fun getInvitationsByEmail(email: String): List<InvitationResponse>
    suspend fun cancelInvitation(projectId: UUID, userId: UUID, invitationId: UUID)
}