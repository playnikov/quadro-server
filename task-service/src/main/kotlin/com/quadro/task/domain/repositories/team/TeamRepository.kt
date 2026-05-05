package com.quadro.task.domain.repositories.team

import com.quadro.task.domain.models.team.Team
import java.util.UUID

interface TeamRepository {
    suspend fun upsert(team: Team)
    suspend fun findById(id: UUID): Team?
    suspend fun delete(id: UUID)
}