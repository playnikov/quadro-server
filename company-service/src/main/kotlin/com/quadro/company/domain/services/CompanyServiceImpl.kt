package com.quadro.company.domain.services

import com.quadro.company.domain.models.Company
import com.quadro.company.domain.models.CompanyCreate
import com.quadro.company.domain.models.CompanyMember
import com.quadro.company.domain.models.CompanyMemberResponse
import com.quadro.company.domain.models.CompanyResponse
import com.quadro.company.domain.models.CompanyRole
import com.quadro.company.domain.models.CompanySettings
import com.quadro.company.domain.models.CompanyStatus
import com.quadro.company.domain.models.CompanyUpdate
import com.quadro.company.domain.repositories.CompanyMemberRepository
import com.quadro.company.domain.repositories.CompanyRepository
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class CompanyServiceImpl(
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
//    private val eventProducer: CompanyEventProducer
) : CompanyService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createCompany(
        userId: UUID,
        request: CompanyCreate
    ): Result<CompanyResponse> {
        return try {
            if (companyRepository.existsByName(request.name)) {
                return Result.failure(Exception("Company with this name already exists"))
            }

            val now = Clock.System.now()
            val company = Company(
                id = UUID.randomUUID(),
                name = request.name,
                description = request.description,
                logo = request.logo,
                website = request.website,
                email = request.email,
                phone = request.phone,
                address = request.address,
                taxId = request.taxId,
                companyStatus = CompanyStatus.ACTIVE,
                ownerId = userId,
                companySettings = request.settings ?: CompanySettings(),
                createdAt = now,
                updatedAt = now,
                currentUsers = 1
            )

            val createdCompany = companyRepository.create(company)

            val member = CompanyMember(
                id = UUID.randomUUID(),
                companyId = createdCompany.id,
                userId = userId,
                role = CompanyRole.OWNER,
                joinedAt = now,
                invitedBy = userId,
                invitedAt = now,
                isActive = true,
                lastActiveAt = now
            )
            companyMemberRepository.add(member)

            logger.info("Company created: ${createdCompany.name} by user: $userId")
            Result.success(CompanyResponse.fromCompany(createdCompany))
        } catch (e: Exception) {
            logger.error("Failed to create company", e)
            Result.failure(e)
        }
    }

    override suspend fun getCompany(
        companyId: UUID,
        userId: UUID
    ): Result<CompanyResponse> {
        return try {
            val company = companyRepository.findById(companyId) ?: return Result.failure(Exception("Company not found"))
            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            Result.success(CompanyResponse.fromCompany(company))
        } catch (e: Exception) {
            logger.error("Failed to get company", e)
            Result.failure(e)
        }
    }

    override suspend fun updateCompany(
        companyId: UUID,
        userId: UUID,
        request: CompanyUpdate
    ): Result<CompanyResponse> {
        return try {
            val company = companyRepository.findById(companyId) ?: return Result.failure(Exception("Company not found"))
            val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
                ?: return Result.failure(Exception("Access denied"))

            if (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            if (request.name != null && request.name != company.name && companyRepository.existsByName(request.name)) {
                return Result.failure(Exception("Company with this name already exists"))
            }

            val now = Clock.System.now()
            val updatedCompany = company.copy(
                name = request.name ?: company.name,
                description = request.description ?: company.description,
                logo = request.logo ?: company.logo,
                website = request.website ?: company.website,
                email = request.email ?: company.email,
                phone = request.phone ?: company.phone,
                address = request.address ?: company.address,
                taxId = request.taxId ?: company.taxId,
                companyStatus = request.status ?: company.companyStatus,
                companySettings = request.settings ?: company.companySettings,
                updatedAt = now
            )
            val saved = companyRepository.update(updatedCompany)
            logger.info("Company updated: ${saved.name}")
            Result.success(CompanyResponse.fromCompany(saved))
        } catch (e: Exception) {
            logger.error("Failed to update company", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCompany(companyId: UUID, userId: UUID): Result<Unit> {
        return try {
            val company = companyRepository.findById(companyId) ?: return Result.failure(Exception("Company not found"))
            if (company.ownerId != userId) {
                return Result.failure(Exception("Only owner can delete company"))
            }
            companyRepository.updateStatus(companyId, CompanyStatus.CLOSED)

            logger.info("Company deleted: $companyId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to delete company", e)
            Result.failure(e)
        }
    }

    override suspend fun getUserCompanies(
        userId: UUID,
        page: Int,
        size: Int
    ): Result<List<CompanyResponse>> {
        return try {
            val offset = (page - 1) * size
            val companies = companyRepository.findByUser(userId, size, offset)
            val result = companies.map { companies ->
                CompanyResponse.fromCompany(companies)
            }
            Result.success(result)
        } catch (e: Exception) {
            logger.error("Failed to get user companies", e)
            Result.failure(e)
        }
    }

    override suspend fun getCompanyMembers(
        companyId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): Result<List<CompanyMemberResponse>> {
        return try {
            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("Access denied"))
            }
            val offset = (page - 1) * size
            val members = companyMemberRepository.findByCompany(companyId, size, offset)
            val result = members.map { member ->
                CompanyMemberResponse.fromCompanyMember(member)
            }
            Result.success(result)
        } catch (e: Exception) {
            logger.error("Failed to get company members", e)
            Result.failure(e)
        }
    }

    override suspend fun updateMemberRole(
        companyId: UUID,
        userId: UUID,
        targetUserId: UUID,
        role: CompanyRole
    ): Result<Unit> {
        return try {
            val currentUser = companyMemberRepository.findByCompanyAndUser(companyId, userId)
                ?: return Result.failure(Exception("Access denied"))
            if (currentUser.role != CompanyRole.OWNER && currentUser.role != CompanyRole.ADMIN) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
                ?: return Result.failure(Exception("User is not a member"))
            if (currentUser.role == CompanyRole.ADMIN && targetMember.role == CompanyRole.ADMIN) {
                return Result.failure(Exception("Admin cannot change another admin's role"))
            }

            companyMemberRepository.updateRole(targetMember.id, role)
            logger.info("Member role updated: $targetUserId to $role")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to update member role", e)
            Result.failure(e)
        }
    }

    override suspend fun removeMember(
        companyId: UUID,
        userId: UUID,
        targetUserId: UUID
    ): Result<Unit> {
        return try {
            val currentUser = companyMemberRepository.findByCompanyAndUser(companyId, userId)
                ?: return Result.failure(Exception("Access denied"))
            val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
                ?: return Result.failure(Exception("User is not a member"))

            if (targetMember.role == CompanyRole.OWNER) {
                return Result.failure(Exception("Cannot remove owner"))
            }
            if (currentUser.role == CompanyRole.ADMIN &&
                targetMember.role != CompanyRole.MEMBER && targetMember.role != CompanyRole.GUEST
            ) {
                return Result.failure(Exception("Admin can only remove members and guests"))
            }

            companyMemberRepository.remove(targetMember.id)
            companyRepository.decrementUserCount(companyId)

            logger.info("Member removed: $targetUserId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to remove member", e)
            Result.failure(e)
        }
    }

    override suspend fun leaveCompany(companyId: UUID, userId: UUID): Result<Unit> {
        return try {
            val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
                ?: return Result.failure(Exception("Not a member"))
            if (member.role == CompanyRole.OWNER) {
                return Result.failure(Exception("Owner cannot leave the company"))
            }
            companyMemberRepository.remove(member.id)
            companyRepository.decrementUserCount(companyId)
            logger.info("User left company: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to leave company", e)
            Result.failure(e)
        }
    }
}