package com.quadro.team.domain.services

import com.quadro.team.domain.models.TeamMemberResponse
import com.quadro.team.domain.models.TeamRole
import java.util.UUID

interface TeamMemberService {
    suspend fun getMembers(teamId: UUID): List<TeamMemberResponse>
    suspend fun addMember(teamId: UUID, userId: UUID, role: TeamRole, requesterId: UUID): TeamMemberResponse
    suspend fun removeMember(teamId: UUID, memberId: UUID, requesterId: UUID)
    suspend fun changeRole(teamId: UUID, memberId: UUID, role: TeamRole, requesterId: UUID)
}