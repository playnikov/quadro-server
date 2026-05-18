package com.quadro.notification.domain.repositories.team

import java.util.UUID

interface TeamMemberRepository {
    suspend fun upsert(member: com.quadro.notification.domain.models.team.TeamMember)
    suspend fun delete(teamId: UUID, userId: UUID)
    suspend fun deleteByTeam(teamId: UUID)
}