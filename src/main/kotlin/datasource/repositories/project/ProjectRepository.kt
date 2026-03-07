package com.quadro.datasource.repositories.project

import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectStatus
import java.util.UUID

interface ProjectRepository {
    suspend fun create(project: Project): Project
    suspend fun findById(id: UUID): Project?
    suspend fun findByKey(companyId: UUID, key: String): Project?
    suspend fun findByName(companyId: UUID, name: String): Project?
    suspend fun update(project: Project): Project
    suspend fun delete(id: UUID): Boolean

    suspend fun findByCompany(companyId: UUID, limit: Int, offset: Int): List<Project>
    suspend fun findByUser(userId: UUID, companyId: UUID?): List<Project>
    suspend fun findByTeam(teamId: UUID): List<Project>

    suspend fun countByCompany(companyId: UUID): Long
    suspend fun countByUser(userId: UUID, companyId: UUID?): Long

    suspend fun existsByKey(companyId: UUID, key: String): Boolean
    suspend fun existsByName(companyId: UUID, name: String): Boolean

    suspend fun updateStatus(id: UUID, status: ProjectStatus): Boolean

    suspend fun search(companyId: UUID, query: String, limit: Int): List<Project>
}