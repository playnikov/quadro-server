package com.quadro.project.domain.services

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectCreate
import com.quadro.project.domain.models.ProjectStatus
import com.quadro.project.domain.models.ProjectUpdate
import java.util.UUID

interface ProjectService {
    suspend fun createProject(userId: UUID, request: ProjectCreate): Project
    suspend fun updateProject(userId: UUID, projectId: UUID, request: ProjectUpdate): Project
    suspend fun deleteProject(userId: UUID, projectId: UUID)

    suspend fun findById(projectId: UUID): Project
    suspend fun findByName(companyId: UUID, name: String): Project
    suspend fun findByKey(companyId: UUID, key: String): Project
    suspend fun findByUser(userId: UUID, companyId: UUID?, limit: Int, offset: Int): List<Project>
    suspend fun findByCompany(companyId: UUID, limit: Int, offset: Int): List<Project>

    suspend fun updateStatus(userId: UUID, projectId: UUID, status: ProjectStatus): Boolean
}