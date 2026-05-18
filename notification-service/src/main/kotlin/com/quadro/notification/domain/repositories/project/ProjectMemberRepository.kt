package com.quadro.notification.domain.repositories.project

import java.util.UUID

interface ProjectMemberRepository {
    suspend fun upsert(member: com.quadro.notification.domain.models.project.ProjectMember)
    suspend fun delete(projectId: UUID, userId: UUID)
    suspend fun deleteByProject(projectId: UUID)
    suspend fun deleteByUserId(userId: UUID)
}