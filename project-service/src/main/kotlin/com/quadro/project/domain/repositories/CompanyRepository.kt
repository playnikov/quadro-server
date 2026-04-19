package com.quadro.project.domain.repositories

import com.quadro.project.domain.models.Company
import java.util.UUID

interface CompanyRepository {
    suspend fun upsert(company: Company): Company
    suspend fun findById(id: UUID): Company?
    suspend fun delete(id: UUID): Boolean
    suspend fun incrementProjectCount(id: UUID): Boolean
    suspend fun decrementProjectCount(id: UUID): Boolean
}