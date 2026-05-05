package com.quadro.task.domain.repositories.task

import com.quadro.task.domain.models.task.Sprint
import java.util.UUID

interface SprintRepository {
    suspend fun create(sprint: Sprint): Sprint
    suspend fun findById(id: UUID): Sprint?
    suspend fun findByProjectId(projectId: UUID): List<Sprint>
    suspend fun update(sprint: Sprint): Sprint
    suspend fun delete(id: UUID)
}