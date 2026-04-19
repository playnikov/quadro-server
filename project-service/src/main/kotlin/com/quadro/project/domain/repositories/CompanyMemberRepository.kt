package com.quadro.project.domain.repositories

import com.quadro.project.domain.models.CompanyMember
import java.util.UUID

interface CompanyMemberRepository {
    suspend fun upsert(member: CompanyMember): CompanyMember
    suspend fun findByCompanyAndUser(companyId: UUID, userId: UUID): CompanyMember?
    suspend fun delete(id: UUID): Boolean
}