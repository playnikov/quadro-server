package com.quadro.task.domain.services

import com.quadro.shared.dto.DomainException
import com.quadro.task.domain.models.task.Sprint
import com.quadro.task.domain.models.task.SprintCreate
import com.quadro.task.domain.models.task.SprintUpdate
import com.quadro.task.domain.repositories.task.SprintRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class SprintServiceImpl(
    private val sprintRepository: SprintRepository
) : SprintService {
    override suspend fun createSprint(sprintCreate: SprintCreate): Sprint {
        val now = Clock.System.now()

        val sprint = Sprint(
            id = UUID.randomUUID(),
            projectId = sprintCreate.projectId,
            name = sprintCreate.name,
            goal = sprintCreate.goal,
            status = sprintCreate.status,
            startDate = sprintCreate.startDate ?: now,
            endDate = sprintCreate.endDate ?: (now + 7.days),
            createdBy = sprintCreate.createdBy,
            createdAt = now,
            updatedAt = now
        )

        return sprintRepository.create(sprint)
    }

    override suspend fun updateSprint(
        id: UUID,
        sprintUpdate: SprintUpdate
    ): Sprint {
        val sprint = sprintRepository.findById(id)
            ?: throw DomainException.NotFound("Sprint", id.toString())

        val updatedSprint = sprint.copy(
            name = sprintUpdate.name ?: sprint.name,
            goal = sprintUpdate.goal ?: sprint.goal,
            status = sprintUpdate.status ?: sprint.status,
            startDate = sprintUpdate.startDate ?: sprint.startDate,
            endDate = sprintUpdate.endDate ?: sprint.endDate,
            updatedAt = Clock.System.now()
        )

        return sprintRepository.update(sprint)
    }

    override suspend fun deleteSprint(id: UUID) {
        val sprint = sprintRepository.findById(id)
            ?: throw DomainException.NotFound("Sprint", id.toString())

        sprintRepository.delete(sprint.id)
    }

    override suspend fun getSprint(id: UUID): Sprint? {
        return sprintRepository.findById(id)
    }

    override suspend fun getSprintsByProject(projectId: UUID): List<Sprint> {
        return sprintRepository.findByProjectId(projectId)
    }
}