package com.quadro.domain.services.team

import com.quadro.domain.models.team.AddTeamMembersRequest
import com.quadro.domain.models.team.TeamCreate
import com.quadro.domain.models.team.TeamMemberResult
import com.quadro.domain.models.team.TeamMemberStats
import com.quadro.domain.models.team.TeamResult
import com.quadro.domain.models.team.TeamRole
import com.quadro.domain.models.team.TeamUpdate
import java.util.UUID

interface TeamService {
    // Team CRUD
    suspend fun createTeam(userId: UUID, request: TeamCreate): Result<TeamResult>
    suspend fun getTeam(teamId: UUID, userId: UUID): Result<TeamResult>
    suspend fun updateTeam(teamId: UUID, userId: UUID, request: TeamUpdate): Result<TeamResult>
    suspend fun deleteTeam(teamId: UUID, userId: UUID): Result<Unit>
    suspend fun archiveTeam(teamId: UUID, userId: UUID): Result<Unit>
    suspend fun restoreTeam(teamId: UUID, userId: UUID): Result<Unit>

    // Team listing
    suspend fun getCompanyTeams(companyId: UUID, userId: UUID, page: Int, size: Int): Result<List<TeamResult>>
    suspend fun getUserTeams(userId: UUID, companyId: UUID?): Result<List<TeamResult>>
    suspend fun searchTeams(companyId: UUID, userId: UUID, query: String): Result<List<TeamResult>>

    // Members management
    suspend fun addMembers(teamId: UUID, userId: UUID, request: AddTeamMembersRequest): Result<List<TeamMemberResult>>
    suspend fun getTeamMembers(teamId: UUID, userId: UUID, page: Int, size: Int): Result<List<TeamMemberResult>>
    suspend fun getTeamMember(teamId: UUID, userId: UUID, targetUserId: UUID): Result<TeamMemberResult>
    suspend fun updateMemberRole(teamId: UUID, userId: UUID, targetUserId: UUID, role: TeamRole): Result<Unit>
    suspend fun removeMember(teamId: UUID, userId: UUID, targetUserId: UUID): Result<Unit>
    suspend fun leaveTeam(teamId: UUID, userId: UUID): Result<Unit>
    suspend fun getTeamStats(teamId: UUID, userId: UUID): Result<TeamMemberStats>
}