package com.quadro.team.domain.services

import com.quadro.team.domain.models.TeamCreate
import com.quadro.team.domain.models.TeamMemberResponse
import com.quadro.team.domain.models.TeamResponse
import com.quadro.team.domain.models.TeamRole
import com.quadro.team.domain.models.TeamUpdate
import java.util.UUID

interface TeamService {
    suspend fun create(companyId: UUID, createdBy: UUID, request: TeamCreate): TeamResponse
    suspend fun getById(id: UUID): TeamResponse
    suspend fun getByCompany(companyId: UUID, page: Int, size: Int): List<TeamResponse>
    suspend fun update(id: UUID, request: TeamUpdate, requesterId: UUID): TeamResponse
    suspend fun delete(id: UUID, requesterId: UUID)
}