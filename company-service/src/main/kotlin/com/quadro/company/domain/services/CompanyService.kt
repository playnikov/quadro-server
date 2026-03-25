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
    suspend fun createCompany(userId: UUID, request: CompanyCreate): Result<CompanyResponse>
    suspend fun getCompany(companyId: UUID, userId: UUID): Result<CompanyResponse>
    suspend fun updateCompany(companyId: UUID, userId: UUID, request: CompanyUpdate): Result<CompanyResponse>
    suspend fun deleteCompany(companyId: UUID, userId: UUID): Result<Unit>
    suspend fun getUserCompanies(userId: UUID, page: Int, size: Int): Result<List<CompanyResponse>>

    suspend fun getCompanyMembers(companyId: UUID, userId: UUID, page: Int, size: Int): Result<List<CompanyMemberResponse>>
    suspend fun updateMemberRole(companyId: UUID, userId: UUID, targetUserId: UUID, role: CompanyRole): Result<Unit>
    suspend fun removeMember(companyId: UUID, userId: UUID, targetUserId: UUID): Result<Unit>
    suspend fun leaveCompany(companyId: UUID, userId: UUID): Result<Unit>
}