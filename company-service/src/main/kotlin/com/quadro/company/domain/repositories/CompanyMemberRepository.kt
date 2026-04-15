package com.quadro.company.domain.repositories

import com.quadro.company.domain.models.CompanyMember
import com.quadro.company.domain.models.CompanyRole
import java.util.UUID
import kotlin.time.Instant

interface CompanyMemberRepository {
    suspend fun findById(id: UUID): CompanyMember?
    suspend fun findByCompanyAndUser(companyId: UUID, userId: UUID): CompanyMember?
    suspend fun findByCompany(companyId: UUID, limit: Int, offset: Int): List<CompanyMember>
    suspend fun findByCompanyAndRole(companyId: UUID, role: CompanyRole): List<CompanyMember>
    suspend fun exists(companyId: UUID, userId: UUID): Boolean
    suspend fun add(member: CompanyMember): CompanyMember
    suspend fun remove(id: UUID)
    suspend fun updateRole(id: UUID, role: CompanyRole)
    suspend fun updateLastActive(companyId: UUID, userId: UUID)
    suspend fun countByCompany(companyId: UUID): Long
    suspend fun findNewMembers(companyId: UUID, since: Instant): List<CompanyMember>
}