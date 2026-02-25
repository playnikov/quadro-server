package com.quadro.datasource.repositories

import com.quadro.domain.models.CompanyMember
import com.quadro.domain.models.CompanyRole
import java.util.UUID

interface CompanyMemberRepository {
    suspend fun add(member: CompanyMember): CompanyMember
    suspend fun findById(id: UUID): CompanyMember?
    suspend fun findByCompanyAndUser(companyId: UUID, userId: UUID): CompanyMember?
    suspend fun findByCompany(companyId: UUID, limit: Int, offset: Int): List<CompanyMember>
    suspend fun findByUser(userId: UUID, limit: Int, offset: Int): List<CompanyMember>
    suspend fun updateRole(id: UUID, role: CompanyRole): Boolean
    suspend fun remove(id: UUID): Boolean
    suspend fun removeByCompanyAndUser(companyId: UUID, userId: UUID): Boolean
    suspend fun countByCompany(companyId: UUID): Long
    suspend fun countByUser(userId: UUID): Long
    suspend fun exists(companyId: UUID, userId: UUID): Boolean
    suspend fun isUserInRole(companyId: UUID, userId: UUID, role: CompanyRole): Boolean
    suspend fun getCompanyOwners(companyId: UUID): List<CompanyMember>
}