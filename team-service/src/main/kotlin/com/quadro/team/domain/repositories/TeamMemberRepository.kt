package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.TeamMember
import com.quadro.team.domain.models.TeamRole
import java.util.UUID

interface TeamMemberRepository {
    suspend fun findByTeamAndUser(teamId: UUID, userId: UUID): TeamMember?
    suspend fun findByTeam(teamId: UUID): List<TeamMember>
    suspend fun exists(teamId: UUID, userId: UUID): Boolean
    suspend fun add(member: TeamMember): TeamMember
    suspend fun remove(id: UUID): Boolean
    suspend fun updateRole(id: UUID, role: TeamRole): Unit
}