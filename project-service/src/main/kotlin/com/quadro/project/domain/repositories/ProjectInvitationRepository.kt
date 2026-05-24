package com.quadro.project.domain.repositories

import com.quadro.project.domain.models.InviteStatus
import com.quadro.project.domain.models.ProjectInvitation
import java.util.UUID

interface ProjectInvitationRepository {
    suspend fun create(invitation: ProjectInvitation): ProjectInvitation
    suspend fun findById(id: UUID): ProjectInvitation?
    suspend fun findByToken(token: String): ProjectInvitation?
    suspend fun findByProject(projectId: UUID): List<ProjectInvitation>
    suspend fun findByEmail(email: String): List<ProjectInvitation>

    suspend fun updateStatus(id: UUID, status: InviteStatus): Boolean
    suspend fun acceptInvitation(id: UUID, userId: UUID): Boolean
    suspend fun delete(id: UUID): Boolean
    suspend fun deleteExpired(): Int

    suspend fun countPendingByProject(projectId: UUID): Long
}