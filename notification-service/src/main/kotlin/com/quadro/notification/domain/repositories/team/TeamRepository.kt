package com.quadro.notification.domain.repositories.team

import java.util.UUID

interface TeamRepository {
    suspend fun upsert(team: com.quadro.notification.domain.models.team.Team)
    suspend fun delete(id: UUID)
}