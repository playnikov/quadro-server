package com.quadro.domain.services

import com.quadro.datasource.repositories.CompanyInvitationRepository
import com.quadro.datasource.repositories.CompanyMemberRepository
import com.quadro.datasource.repositories.CompanyRepository
import com.quadro.datasource.repositories.UserRepository
import com.quadro.domain.models.*
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

class CompanyServiceImpl(
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val companyInvitationRepository: CompanyInvitationRepository,
    private val userRepository: UserRepository
) : CompanyService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun createCompany(
        userId: UUID,
        request: CompanyCreate
    ): Result<CompanyResponse> {
        return try {
            if (companyRepository.existsByName(request.name)) {
                return Result.failure(Exception("Company with this name already exists"))
            }

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
                companySettings = request.settings ?: CompanySettings()
            )

            val createdCompany = companyRepository.create(company)

            val member = CompanyMember(
                id = UUID.randomUUID(),
                companyId = createdCompany.id,
                userId = userId,
                role = CompanyRole.OWNER,
                joinedAt = Instant.now().toEpochMilli(),
                invitedBy = userId,
                isActive = true
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
            val company = companyRepository.findById(companyId)
                ?: return Result.failure(Exception("Company not found"))

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
            val company = companyRepository.findById(companyId)
                ?: return Result.failure(Exception("Company not found"))

            val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)

            if (member == null || (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN)) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            if (request.name != null && request.name != company.name) {
                if (companyRepository.existsByName(request.name)) {
                    return Result.failure(Exception("Company with this name already exists"))
                }
            }

            val updatedCompany = company.copy(
                name = request.name ?: company.name,
                description = request.description ?: company.description,
                logo = request.logo ?: company.logo,
                website = request.website ?: company.website,
                email = request.email ?: company.email,
                phone = request.phone ?: company.phone,
                address = request.address ?: company.address,
                taxId = request.taxId ?: company.taxId,
                companySettings = request.settings ?: company.companySettings,
                companyStatus = request.status ?: company.companyStatus,
                updatedAt = Instant.now().toEpochMilli()
            )

            val savedCompany = companyRepository.update(updatedCompany)
            logger.info("Company updated: ${savedCompany.name} by user: $userId")

            Result.success(CompanyResponse.fromCompany(savedCompany))
        } catch (e: Exception) {
            logger.error("Failed to update company", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteCompany(companyId: UUID, userId: UUID): Result<Unit> {
        return try {
            val company = companyRepository.findById(companyId)
                ?: return Result.failure(Exception("Company not found"))

            if (company.ownerId != userId) {
                return Result.failure(Exception("Only owner can delete company"))
            }

            companyRepository.updateStatus(companyId, CompanyStatus.CLOSED)
            logger.info("Company deleted: $companyId by user: $userId")

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
            Result.success(companies.map { CompanyResponse.fromCompany(it) })
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

            val responses = members.map { member ->
                val user = userRepository.findById(member.userId)
                    ?: return@map null

                val invitedBy = userRepository.findById(member.invitedBy)

                CompanyMemberResponse(
                    id = member.id,
                    companyId = member.companyId,
                    userId = member.userId,
                    userEmail = user.email,
                    userName = user.username,
                    userAvatar = user.avatar,
                    role = member.role,
                    joinedAt = member.joinedAt,
                    invitedBy = member.invitedBy,
                    invitedByEmail = invitedBy?.email ?: "unknown",
                    isActive = member.isActive
                )
            }.filterNotNull()

            Result.success(responses)
        } catch (e: Exception) {
            logger.error("Failed to get company members", e)
            Result.failure(e)
        }
    }

    override suspend fun updateMemberRole(
        companyId: UUID,
        userId: UUID,
        targetUserId: UUID,
        request: UpdateMemberRole
    ): Result<Unit> {
        return try {
            val currentUserMember = companyMemberRepository.findByCompanyAndUser(companyId, userId)
                ?: return Result.failure(Exception("Access denied"))

            if (currentUserMember.role != CompanyRole.OWNER && currentUserMember.role != CompanyRole.ADMIN) {
                return Result.failure(Exception("Insufficient permissions"))
            }

            val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
                ?: return Result.failure(Exception("User is not a member of this company"))

            if (currentUserMember.role == CompanyRole.ADMIN && targetMember.role == CompanyRole.ADMIN) {
                return Result.failure(Exception("Admin cannot change another admin's role"))
            }

            companyMemberRepository.updateRole(targetMember.id, request.role)
            logger.info("Member role updated: $targetUserId to ${request.role} in company: $companyId")

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
            val currentUserMember = companyMemberRepository.findByCompanyAndUser(companyId, userId)
                ?: return Result.failure(Exception("Access denied"))

            val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
                ?: return Result.failure(Exception("User is not a member of this company"))

            if (targetMember.role == CompanyRole.OWNER) {
                return Result.failure(Exception("Cannot remove owner"))
            }

            if (currentUserMember.role == CompanyRole.ADMIN &&
                targetMember.role != CompanyRole.MEMBER &&
                targetMember.role != CompanyRole.GUEST
            ) {
                return Result.failure(Exception("Admin can only remove members and guests"))
            }

            companyMemberRepository.remove(targetMember.id)

            logger.info("Member removed: $targetUserId from company: $companyId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to remove member", e)
            Result.failure(e)
        }
    }

    override suspend fun leaveCompany(companyId: UUID, userId: UUID): Result<Unit> {
        return try {
            val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
                ?: return Result.failure(Exception("Not a member of this company"))

            if (member.role == CompanyRole.OWNER) {
                return Result.failure(Exception("Owner cannot leave the company. Transfer ownership first or delete the company."))
            }

            companyMemberRepository.remove(member.id)

            logger.info("User left company: $userId from company: $companyId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to leave company", e)
            Result.failure(e)
        }
    }
}