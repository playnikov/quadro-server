package com.quadro.task.domain.repositories.team

import com.quadro.task.domain.models.team.TeamMember
import java.util.UUID

interface TeamMemberRepository {
    suspend fun upsert(member: TeamMember)
    suspend fun findByTeam(teamId: UUID): List<TeamMember>
    suspend fun findByUserId(userId: UUID): List<TeamMember>
    suspend fun deleteByTeam(teamId: UUID)
    suspend fun delete(teamId: UUID, userId: UUID)
}