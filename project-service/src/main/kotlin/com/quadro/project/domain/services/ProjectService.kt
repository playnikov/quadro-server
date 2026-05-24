package com.quadro.project.domain.services

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectCreate
import com.quadro.project.domain.models.ProjectMemberResponse
import com.quadro.project.domain.models.MemberRole
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectUpdate
import java.util.UUID

interface ProjectService {
    suspend fun createProject(userId: UUID, request: ProjectCreate): Project
    suspend fun updateProject(userId: UUID, projectId: UUID, request: ProjectUpdate): Project
    suspend fun deleteProject(userId: UUID, projectId: UUID)

    suspend fun findById(projectId: UUID): Project
    suspend fun findByName(name: String): Project
    suspend fun findByKey(key: String): Project
    suspend fun findByUser(userId: UUID, limit: Int, offset: Int): List<Project>

    suspend fun updateStatus(userId: UUID, projectId: UUID, status: ProjectStatus): Boolean

    suspend fun getProjectMembers(projectId: UUID, userId: UUID, page: Int, size: Int): List<ProjectMemberResponse>
    suspend fun updateMemberRole(projectId: UUID, userId: UUID, targetUserId: UUID, role: MemberRole)
    suspend fun removeMember(projectId: UUID, userId: UUID, targetUserId: UUID)
    suspend fun leaveProject(projectId: UUID, userId: UUID)
}