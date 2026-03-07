package com.quadro.datasource.repositories.team

import com.quadro.domain.models.team.TeamMember
import com.quadro.domain.models.team.TeamMemberStats
import com.quadro.domain.models.team.TeamRole
import java.util.UUID

interface TeamMemberRepository {
    suspend fun add(member: TeamMember): TeamMember
    suspend fun addAll(members: List<TeamMember>): List<TeamMember>
    suspend fun findById(id: UUID): TeamMember?
    suspend fun findByTeamAndUser(teamId: UUID, userId: UUID): TeamMember?
    suspend fun findByTeam(teamId: UUID, limit: Int, offset: Int): List<TeamMember>
    suspend fun findByUser(userId: UUID, companyId: UUID?): List<TeamMember>

    suspend fun updateRole(id: UUID, role: TeamRole): Boolean
    suspend fun updateLastActive(id: UUID): Boolean
    suspend fun remove(id: UUID): Boolean
    suspend fun removeByTeamAndUser(teamId: UUID, userId: UUID): Boolean
    suspend fun removeAllByTeam(teamId: UUID): Int

    suspend fun countByTeam(teamId: UUID): Long
    suspend fun countByUser(userId: UUID, companyId: UUID?): Long
    suspend fun exists(teamId: UUID, userId: UUID): Boolean
    suspend fun isUserInRole(teamId: UUID, userId: UUID, role: TeamRole): Boolean

    suspend fun getTeamLeads(teamId: UUID): List<TeamMember>
    suspend fun getTeamAdmins(teamId: UUID): List<TeamMember>
    suspend fun getActiveToday(teamId: UUID): List<TeamMember>
    suspend fun getStats(teamId: UUID): TeamMemberStats
}