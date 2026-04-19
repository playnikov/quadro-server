package com.quadro.project.domain.repositories

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectStatus
import java.util.UUID

interface ProjectRepository {
    suspend fun create(project: Project): Project
    suspend fun findById(id: UUID): Project?
    suspend fun findByKey(companyId: UUID, key: String): Project?
    suspend fun findByName(companyId: UUID, name: String): Project?
    suspend fun update(project: Project): Project
    suspend fun delete(id: UUID): Boolean

    suspend fun findByCompany(companyId: UUID, limit: Int, offset: Int): List<Project>
    suspend fun findByUser(userId: UUID, companyId: UUID?, limit: Int, offset: Int): List<Project>

    suspend fun existsByKey(companyId: UUID, key: String): Boolean
    suspend fun existsByName(companyId: UUID, name: String): Boolean

    suspend fun updateStatus(id: UUID, status: ProjectStatus): Boolean
}