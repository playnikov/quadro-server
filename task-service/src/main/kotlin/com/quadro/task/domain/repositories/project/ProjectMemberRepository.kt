package com.quadro.task.domain.repositories.project

import com.quadro.task.domain.models.project.ProjectMember
import java.util.UUID

interface ProjectMemberRepository {
    suspend fun upsert(member: ProjectMember)
    suspend fun findByProjectAndUser(projectId: UUID, userId: UUID): ProjectMember?
    suspend fun findByProjectId(projectId: UUID): List<ProjectMember>
    suspend fun findByUserId(userId: UUID): List<ProjectMember>
    suspend fun delete(projectId: UUID, userId: UUID)
    suspend fun deleteByUserId(userId: UUID)
    suspend fun deleteByProject(projectId: UUID)
}