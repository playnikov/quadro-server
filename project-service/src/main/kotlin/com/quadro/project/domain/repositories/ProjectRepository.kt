package com.quadro.project.domain.repositories

import com.quadro.project.domain.models.Project
import com.quadro.project.domain.models.ProjectStatus
import java.util.UUID

interface ProjectRepository {
    suspend fun create(project: Project): Project
    suspend fun findById(id: UUID): Project?
    suspend fun findByKey(key: String): Project?
    suspend fun findByName(name: String): Project?
    suspend fun update(project: Project): Project
    suspend fun delete(id: UUID): Boolean

    suspend fun findByUser(userId: UUID, limit: Int, offset: Int): List<Project>

    suspend fun existsByName(name: String): Boolean
    suspend fun existsByKey(key: String): Boolean

    suspend fun updateStatus(id: UUID, status: ProjectStatus): Boolean
}