package com.quadro.domain.services

import com.quadro.domain.models.CompanyCreate
import com.quadro.domain.models.CompanyMemberResponse
import com.quadro.domain.models.CompanyResponse
import com.quadro.domain.models.CompanyUpdate
import com.quadro.domain.models.UpdateMemberRole
import java.util.UUID

interface CompanyService {
    suspend fun createCompany(userId: UUID, request: CompanyCreate): Result<CompanyResponse>
    suspend fun getCompany(companyId: UUID, userId: UUID): Result<CompanyResponse>
    suspend fun updateCompany(companyId: UUID, userId: UUID, request: CompanyUpdate): Result<CompanyResponse>
    suspend fun deleteCompany(companyId: UUID, userId: UUID): Result<Unit>
    suspend fun getUserCompanies(userId: UUID, page: Int, size: Int): Result<List<CompanyResponse>>
    suspend fun getCompanyMembers(companyId: UUID, userId: UUID, page: Int, size: Int): Result<List<CompanyMemberResponse>>
    suspend fun updateMemberRole(companyId: UUID, userId: UUID, targetUserId: UUID, request: UpdateMemberRole): Result<Unit>
    suspend fun removeMember(companyId: UUID, userId: UUID, targetUserId: UUID): Result<Unit>
    suspend fun leaveCompany(companyId: UUID, userId: UUID): Result<Unit>
}