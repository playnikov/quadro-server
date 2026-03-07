package com.quadro.domain.services.company

import com.quadro.domain.models.company.CompanyCreate
import com.quadro.domain.models.company.CompanyMemberResult
import com.quadro.domain.models.company.CompanyResult
import com.quadro.domain.models.company.CompanyUpdate
import com.quadro.domain.models.company.UpdateCompanyMemberRole
import java.util.UUID

interface CompanyService {
    suspend fun createCompany(userId: UUID, request: CompanyCreate): Result<CompanyResult>
    suspend fun getCompany(companyId: UUID, userId: UUID): Result<CompanyResult>
    suspend fun updateCompany(companyId: UUID, userId: UUID, request: CompanyUpdate): Result<CompanyResult>
    suspend fun deleteCompany(companyId: UUID, userId: UUID): Result<Unit>
    suspend fun getUserCompanies(userId: UUID, page: Int, size: Int): Result<List<CompanyResult>>
    suspend fun getCompanyMembers(companyId: UUID, userId: UUID, page: Int, size: Int): Result<List<CompanyMemberResult>>
    suspend fun updateMemberRole(companyId: UUID, userId: UUID, targetUserId: UUID, request: UpdateCompanyMemberRole): Result<Unit>
    suspend fun removeMember(companyId: UUID, userId: UUID, targetUserId: UUID): Result<Unit>
    suspend fun leaveCompany(companyId: UUID, userId: UUID): Result<Unit>
}