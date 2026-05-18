package com.quadro.notification.domain.repositories.project

import com.quadro.notification.domain.models.project.Project

interface ProjectRepository {
    suspend fun upsert(project: Project)
    suspend fun delete(id: java.util.UUID)
}