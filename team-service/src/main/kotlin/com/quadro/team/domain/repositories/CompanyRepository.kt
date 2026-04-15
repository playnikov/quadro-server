package com.quadro.team.domain.repositories

import com.quadro.team.domain.models.Company
import java.util.UUID

interface CompanyRepository {
    suspend fun upsert(company: Company): Company
    suspend fun findById(id: UUID): Company?
    suspend fun delete(id: UUID): Boolean
}