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
import com.quadro.company.domain.repositories.UserRepository
import com.quadro.shared.events.CompanyCreatedEvent
import com.quadro.shared.events.CompanyDeletedEvent
import com.quadro.shared.events.CompanyMemberAddedEvent
import com.quadro.shared.events.CompanyMemberRemovedEvent
import com.quadro.shared.events.CompanyMemberRoleUpdatedEvent
import com.quadro.shared.events.CompanyUpdatedEvent
import com.quadro.shared.kafka.EventProducer
import com.quadro.shared.kafka.KafkaTopics
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Clock

class CompanyServiceImpl(
    private val companyRepository: CompanyRepository,
    private val companyMemberRepository: CompanyMemberRepository,
    private val userRepository: UserRepository,
    private val eventProducer: EventProducer
) : CompanyService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun createCompany(
        userId: UUID,
        request: CompanyCreate
    ): Result<CompanyResponse> {
        return try {
            val user = userRepository.findById(userId) ?: return Result.failure(Exception("User not found"))

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

            eventProducer.publish(
                topic = KafkaTopics.COMPANY_CREATED,
                key = createdCompany.id.toString(),
                event = CompanyCreatedEvent(
                    companyId = createdCompany.id.toString(),
                    name = createdCompany.name,
                    ownerId = createdCompany.ownerId.toString(),
                    maxProjects = createdCompany.maxProjects,
                    maxMembers = createdCompany.maxUsers
                )
            )

            logger.info("Company created: ${createdCompany.name} by user: $userId")
            Result.success(CompanyResponse.fromCompany(createdCompany, user))
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
            val user = userRepository.findById(userId) ?: return Result.failure(Exception("User not found"))
            val company = companyRepository.findById(companyId) ?: return Result.failure(Exception("Company not found"))
            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("Access denied"))
            }

            val owner = userRepository.findById(company.ownerId)
            Result.success(CompanyResponse.fromCompany(company, owner))
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
            val user = userRepository.findById(userId) ?: return Result.failure(Exception("User not found"))
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

            eventProducer.publish(
                topic = KafkaTopics.COMPANY_UPDATED,
                key = updatedCompany.id.toString(),
                event = CompanyUpdatedEvent(
                    companyId = updatedCompany.id.toString(),
                    name = updatedCompany.name,
                    ownerId = updatedCompany.ownerId.toString(),
                    maxProjects = updatedCompany.maxProjects,
                    maxMembers = updatedCompany.maxUsers
                )
            )

            logger.info("Company updated: ${saved.name}")
            Result.success(CompanyResponse.fromCompany(saved, user))
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
            companyRepository.delete(companyId)

            eventProducer.publish(
                topic = KafkaTopics.COMPANY_DELETED,
                key = company.id.toString(),
                event = CompanyDeletedEvent(
                    companyId = companyId.toString(),
                )
            )

            logger.info("Company deleted: $companyId by owner: $userId")
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
            val user = userRepository.findById(userId) ?: return Result.failure(Exception("User not found"))
            val offset = (page - 1) * size
            val companies = companyRepository.findByUser(userId, size, offset)
            val result = companies.map { company ->
                val owner = userRepository.findById(company.ownerId)
                CompanyResponse.fromCompany(company, owner)
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
            require(page >= 1) { "Page must be >= 1" }
            require(size in 1..100) { "Size must be between 1 and 100" }

            if (!companyMemberRepository.exists(companyId, userId)) {
                return Result.failure(Exception("Access denied"))
            }
            val offset = (page - 1) * size
            val result = companyMemberRepository
                .findByCompany(companyId, size, offset)
                .map { CompanyMemberResponse.fromCompanyMember(it) }
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
            if (userId == targetUserId) {
                return Result.failure(Exception("Cannot change your own role"))
            }

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

            eventProducer.publish(
                topic = KafkaTopics.COMPANY_MEMBER_ROLE_UPDATED,
                key = targetMember.id.toString(),
                event = CompanyMemberRoleUpdatedEvent(
                    companyId = companyId.toString(),
                    userId = targetUserId.toString(),
                    role = role.name
                )
            )

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

            eventProducer.publish(
                topic = KafkaTopics.COMPANY_MEMBER_REMOVED,
                key = targetMember.id.toString(),
                event = CompanyMemberRemovedEvent(
                    companyId = companyId.toString(),
                    userId = userId.toString()
                )
            )

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

            eventProducer.publish(
                topic = KafkaTopics.COMPANY_MEMBER_REMOVED,
                key = member.id.toString(),
                event = CompanyMemberRemovedEvent(
                    companyId = companyId.toString(),
                    userId = userId.toString()
                )
            )

            logger.info("User left company: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Failed to leave company", e)
            Result.failure(e)
        }
    }
}