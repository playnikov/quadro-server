package com.quadro.datasource.repositories.team

import com.quadro.domain.models.team.Team
import com.quadro.domain.models.team.TeamStatus
import com.quadro.domain.models.team.TeamVisibility
import java.util.UUID

class TeamRepositoryImpl : TeamRepository {
    override suspend fun create(team: Team): Team {
        TODO("Not yet implemented")
    }

    override suspend fun findById(id: UUID): Team? {
        TODO("Not yet implemented")
    }

    override suspend fun findByName(companyId: UUID, name: String): Team? {
        TODO("Not yet implemented")
    }

    override suspend fun update(team: Team): Team {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun findByCompany(
        companyId: UUID,
        limit: Int,
        offset: Int
    ): List<Team> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUser(
        userId: UUID,
        companyId: UUID?
    ): List<Team> {
        TODO("Not yet implemented")
    }

    override suspend fun countByCompany(companyId: UUID): Long {
        TODO("Not yet implemented")
    }

    override suspend fun countByUser(userId: UUID, companyId: UUID?): Long {
        TODO("Not yet implemented")
    }

    override suspend fun existsByName(companyId: UUID, name: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateStatus(id: UUID, status: TeamStatus): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun updateVisibility(
        id: UUID,
        visibility: TeamVisibility
    ): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun incrementMemberCount(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun decrementMemberCount(id: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun changeLead(id: UUID, newLeadId: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun search(
        companyId: UUID,
        query: String,
        limit: Int
    ): List<Team> {
        TODO("Not yet implemented")
    }
}