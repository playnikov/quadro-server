package com.quadro.task.domain.repositories.project

import com.quadro.task.domain.models.project.Project
import java.util.UUID

interface ProjectRepository {
    suspend fun upsert(project: Project)
    suspend fun findById(id: UUID): Project?
    suspend fun findByKey(key: String): Project?
    suspend fun findAll(): List<Project>
    suspend fun delete(id: UUID)
}