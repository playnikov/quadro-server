package com.quadro.team.domain.services

import com.quadro.team.domain.models.AddTeamMembersRequest
import com.quadro.team.domain.models.TeamCreate
import com.quadro.team.domain.models.TeamMemberResponse
import com.quadro.team.domain.models.TeamMemberStats
import com.quadro.team.domain.models.TeamResponse
import com.quadro.team.domain.models.TeamUpdate
import com.quadro.team.domain.models.UpdateTeamMemberRole
import java.util.UUID

interface TeamService {
    suspend fun createTeam(companyId: UUID, userId: UUID, request: TeamCreate): Result<TeamResponse>
    suspend fun getTeam(teamId: UUID, userId: UUID): Result<TeamResponse>
    suspend fun updateTeam(teamId: UUID, userId: UUID, request: TeamUpdate): Result<TeamResponse>
    suspend fun deleteTeam(teamId: UUID, userId: UUID): Result<Unit>
    suspend fun archiveTeam(teamId: UUID, userId: UUID): Result<Unit>
    suspend fun restoreTeam(teamId: UUID, userId: UUID): Result<Unit>
    suspend fun getCompanyTeams(companyId: UUID, userId: UUID, page: Int, size: Int): Result<List<TeamResponse>>
    suspend fun getUserTeams(userId: UUID, companyId: UUID?): Result<List<TeamResponse>>
    suspend fun searchTeams(companyId: UUID, userId: UUID, query: String): Result<List<TeamResponse>>

    // Members management
    suspend fun addMembers(teamId: UUID, userId: UUID, request: AddTeamMembersRequest): Result<List<TeamMemberResponse>>
    suspend fun getTeamMembers(teamId: UUID, userId: UUID, page: Int, size: Int): Result<List<TeamMemberResponse>>
    suspend fun getTeamMember(teamId: UUID, userId: UUID, targetUserId: UUID): Result<TeamMemberResponse>
    suspend fun updateMemberRole(teamId: UUID, userId: UUID, targetUserId: UUID, request: UpdateTeamMemberRole): Result<Unit>
    suspend fun removeMember(teamId: UUID, userId: UUID, targetUserId: UUID): Result<Unit>
    suspend fun leaveTeam(teamId: UUID, userId: UUID): Result<Unit>
    suspend fun getTeamStats(teamId: UUID, userId: UUID): Result<TeamMemberStats>
}