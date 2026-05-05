package com.quadro.task.domain.services

import com.quadro.task.domain.models.task.Sprint
import com.quadro.task.domain.models.task.SprintCreate
import com.quadro.task.domain.models.task.SprintUpdate
import java.util.UUID

interface SprintService {
    suspend fun createSprint(sprintCreate: SprintCreate): Sprint
    suspend fun updateSprint(id: UUID, sprintUpdate: SprintUpdate): Sprint
    suspend fun deleteSprint(id: UUID)
    suspend fun getSprint(id: UUID): Sprint?
    suspend fun getSprintsByProject(projectId: UUID): List<Sprint>
}