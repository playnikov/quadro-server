package com.quadro.company.domain.services

import com.quadro.company.domain.models.Company
import com.quadro.company.domain.models.CompanyCreate
import com.quadro.company.domain.models.CompanyMember
import com.quadro.company.domain.models.CompanyMemberResponse
import com.quadro.company.domain.models.CompanyResponse
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.models.CompanyUpdate
import java.util.UUID

interface CompanyService {
    suspend fun createCompany(userId: UUID, request: CompanyCreate): CompanyResponse
    suspend fun getCompany(companyId: UUID, userId: UUID): CompanyResponse
    suspend fun updateCompany(companyId: UUID, userId: UUID, request: CompanyUpdate): CompanyResponse
    suspend fun deleteCompany(companyId: UUID, userId: UUID)
    suspend fun getUserCompanies(userId: UUID, page: Int, size: Int): List<CompanyResponse>

    suspend fun getCompanyMembers(companyId: UUID, userId: UUID, page: Int, size: Int): List<CompanyMemberResponse>
    suspend fun updateMemberRole(companyId: UUID, userId: UUID, targetUserId: UUID, role: CompanyRole)
    suspend fun removeMember(companyId: UUID, userId: UUID, targetUserId: UUID)
    suspend fun leaveCompany(companyId: UUID, userId: UUID)
}