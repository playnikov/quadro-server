package com.quadro.datasource.repositories

import com.quadro.domain.models.Company
import com.quadro.domain.models.CompanyStatus
import java.util.UUID

interface CompanyRepository {
    suspend fun create(company: Company): Company
    suspend fun findById(id: UUID): Company?
    suspend fun findByName(name: String): Company?
    suspend fun update(company: Company): Company
    suspend fun delete(id: UUID): Boolean
    suspend fun findByOwner(ownerId: UUID, limit: Int, offset: Int): List<Company>
    suspend fun findByUser(userId: UUID, limit: Int, offset: Int): List<Company>
    suspend fun countByOwner(ownerId: UUID): Long
    suspend fun countByUser(userId: UUID): Long
    suspend fun existsByName(name: String): Boolean
    suspend fun updateStatus(id: UUID, status: CompanyStatus): Boolean
}