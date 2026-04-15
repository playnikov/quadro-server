package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.Team
import java.util.UUID

interface TeamRepository {
    suspend fun findById(id: UUID): Team?
    suspend fun findByCompany(companyId: UUID, page: Int, size: Int): List<Team>
    suspend fun existsByNameInCompany(companyId: UUID, name: String): Boolean
    suspend fun create(team: Team): Team
    suspend fun update(team: Team): Team
    suspend fun delete(id: UUID): Boolean
}