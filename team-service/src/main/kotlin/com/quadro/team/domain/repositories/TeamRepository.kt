package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.Team
import com.quadro.team.domain.models.TeamStatus
import com.quadro.team.domain.models.TeamVisibility
import java.util.UUID

interface TeamRepository {
    suspend fun create(team: Team): Team
    suspend fun findById(id: UUID): Team?
    suspend fun findByName(companyId: UUID, name: String): Team?
    suspend fun update(team: Team): Team
    suspend fun delete(id: UUID): Boolean
    suspend fun findByCompany(companyId: UUID, limit: Int, offset: Int): List<Team>
    suspend fun findByUser(userId: UUID, companyId: UUID?): List<Team>
    suspend fun countByCompany(companyId: UUID): Long
    suspend fun countByUser(userId: UUID, companyId: UUID?): Long
    suspend fun existsByName(companyId: UUID, name: String): Boolean
    suspend fun updateStatus(id: UUID, status: TeamStatus): Boolean
    suspend fun updateVisibility(id: UUID, visibility: TeamVisibility): Boolean
    suspend fun incrementMemberCount(id: UUID): Boolean
    suspend fun decrementMemberCount(id: UUID): Boolean
    suspend fun changeLead(id: UUID, newLeadId: UUID): Boolean
    suspend fun search(companyId: UUID, query: String, limit: Int): List<Team>
}