package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.Company
import com.quadro.team.domain.models.Project
import java.util.UUID

interface ProjectRepository {
    suspend fun upsert(project: Project): Project
    suspend fun findById(id: UUID): Project?
    suspend fun delete(id: UUID): Boolean
}