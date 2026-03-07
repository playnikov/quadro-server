package com.quadro.datasource.repositories.team

import com.quadro.domain.models.team.TeamMember
import com.quadro.domain.models.team.TeamMemberStats
import com.quadro.domain.models.team.TeamRole
import java.util.UUID

class TeamMemberRepositoryImpl : TeamMemberRepository {
    override suspend fun add(member: TeamMember): TeamMember {
        TODO("Not yet implemented")
    }

    override suspend fun addAll(members: List<TeamMember>): List<TeamMember> {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: UUID): TeamMember? {
        TODO("Not yet implemented")
    }

    override suspend fun findByTeamAndUser(
        teamId: UUID,
        userId: UUID
    ): TeamMember? {
        TODO("Not yet implemented")
    }

    override suspend fun findByTeam(
        teamId: UUID,
        limit: Int,
        offset: Int
    ): List<TeamMember> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?
    ): List<TeamMember> {
        TODO("Not yet implemented")
    }

    override suspend fun updateRole(id: UUID, role: TeamRole): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateLastActive(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun remove(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removeByTeamAndUser(teamId: UUID, userId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removeAllByTeam(teamId: UUID): Int {
        TODO("Not yet implemented")
    }

    override suspend fun countByTeam(teamId: UUID): Long {
        TODO("Not yet implemented")
    }

    override suspend fun countByUser(userId: UUID, companyId: UUID?): Long {
        TODO("Not yet implemented")
    }

    override suspend fun exists(teamId: UUID, userId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun isUserInRole(
        teamId: UUID,
        userId: UUID,
        role: TeamRole
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getTeamLeads(teamId: UUID): List<TeamMember> {
        TODO("Not yet implemented")
    }

    override suspend fun getTeamAdmins(teamId: UUID): List<TeamMember> {
        TODO("Not yet implemented")
    }

    override suspend fun getActiveToday(teamId: UUID): List<TeamMember> {
        TODO("Not yet implemented")
    }

    override suspend fun getStats(teamId: UUID): TeamMemberStats {
        TODO("Not yet implemented")
    }
}