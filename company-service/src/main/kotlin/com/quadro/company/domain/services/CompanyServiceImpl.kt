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
import com.quadro.shared.dto.DomainException
import com.quadro.shared.data.messaging.events.CompanyCreatedEvent
import com.quadro.shared.data.messaging.events.CompanyDeletedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberRemovedEvent
import com.quadro.shared.data.messaging.events.CompanyMemberRoleUpdatedEvent
import com.quadro.shared.data.messaging.events.CompanyUpdatedEvent
import com.quadro.shared.data.messaging.EventProducer
import com.quadro.shared.data.messaging.KafkaTopics
import com.quadro.shared.data.messaging.events.CompanyMemberAddedEvent
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

    private suspend fun userActive(userId: UUID) {
        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", userId.toString())
        if (!user.isActive)
            throw DomainException.AccessDenied()
    }

    override suspend fun createCompany(
        userId: UUID,
        request: CompanyCreate
    ): CompanyResponse {
        userActive(userId)
        request.validate()

        if (companyRepository.existsByName(request.name)) {
            logger.warn("User $userId attempted to create company with existing name: ${request.name}")
            throw DomainException.AlreadyExists("Company '${request.name}'")
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

        eventProducer.publish(
            topic = KafkaTopics.COMPANY_CREATED,
            key = createdCompany.id.toString(),
            event = CompanyCreatedEvent(
                companyId = createdCompany.id.toString(),
                name = createdCompany.name,
                ownerId = createdCompany.ownerId.toString(),
                maxProjects = createdCompany.maxProjects,
                companyStatus = createdCompany.companyStatus.name,
                teamManagementRole = createdCompany.companySettings.teamManagementRole.name,
                manageProjectRole = createdCompany.companySettings.projectManagementRole.name,
            )
        )

        val member = CompanyMember(
            id = UUID.randomUUID(),
            companyId = createdCompany.id,
            userId = userId,
            role = CompanyRole.OWNER,
            joinedAt = now,
            invitedBy = userId,
            invitedAt = now,
            lastActiveAt = now
        )
        companyMemberRepository.add(member)

        eventProducer.publish(
            topic = KafkaTopics.COMPANY_MEMBER_ADDED,
            key = member.id.toString(),
            event = CompanyMemberAddedEvent(
                memberId = member.id.toString(),
                companyId = member.companyId.toString(),
                userId = member.userId.toString(),
                role = member.role.name,
                invitedBy = member.invitedBy.toString()
            )
        )

        logger.info("Company created: ${createdCompany.name} by user: $userId")
        return CompanyResponse.from(createdCompany)
    }

    override suspend fun getCompany(
        companyId: UUID,
        userId: UUID
    ): CompanyResponse {
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())
        companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")

        logger.info("User $userId accessed company ${company.name} (ID: ${company.id})")
        return CompanyResponse.from(company)
    }

    override suspend fun updateCompany(
        companyId: UUID,
        userId: UUID,
        request: CompanyUpdate
    ): CompanyResponse {
        userActive(userId)
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())
        val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member")

        if (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN) {
            logger.warn("User ${userId} with role ${member.role} attempted to update company ${companyId} without sufficient permissions")
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        if (request.name != null && request.name != company.name && companyRepository.existsByName(request.name)) {
            logger.warn("User ${userId} attempted to rename company ${companyId} to existing name: ${request.name}")
            throw DomainException.AlreadyExists("Company '${request.name}'")
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
                maxMembers = updatedCompany.maxUsers,
                companyStatus = updatedCompany.companyStatus.name,
                teamManagementRole = updatedCompany.companySettings.teamManagementRole.name,
                manageProjectRole = updatedCompany.companySettings.projectManagementRole.name,
                currentProjects = updatedCompany.currentProjects
            )
        )

        logger.info("Company updated: ${saved.name} by user: $userId")
        return CompanyResponse.from(saved)
    }

    override suspend fun deleteCompany(companyId: UUID, userId: UUID) {
        userActive(userId)
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())
        if (company.ownerId != userId) {
            logger.warn("User ${userId} attempted to delete company ${companyId} without ownership")
            throw DomainException.AccessDenied("Only owner can delete company")
        }
        companyRepository.delete(companyId)

        eventProducer.publish(
            topic = KafkaTopics.COMPANY_DELETED,
            key = company.id.toString(),
            event = CompanyDeletedEvent(
                companyId = companyId.toString(),
            )
        )

        logger.info("Company deleted: ${company.name} (ID: ${company.id}) by owner: ${userId}")
    }

    override suspend fun getUserCompanies(
        userId: UUID,
        page: Int,
        size: Int
    ): List<CompanyResponse> {
        userActive(userId)
        val offset = (page - 1) * size
        val companies = companyRepository.findByUser(userId, size, offset)

        logger.info("User ${userId} requested list of companies, page: ${page}, size: ${size}, count: ${companies.size}")
        return companies.map { company ->
            CompanyResponse.from(company)
        }
    }

    override suspend fun getCompanyMembers(
        companyId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): List<CompanyMemberResponse> {
        userActive(userId)
        require(page >= 1) { "Page must be >= 1" }
        require(size in 1..100) { "Size must be between 1 and 100" }

        companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")

        val offset = (page - 1) * size
        val members = companyMemberRepository.findByCompany(companyId, size, offset)
        logger.info("User ${userId} requested members of company ${companyId}, page: ${page}, size: ${size}, count: ${members.size}")
        return members.map { member ->
            CompanyMemberResponse.fromCompanyMember(member)
        }
    }

    override suspend fun updateMemberRole(
        companyId: UUID,
        userId: UUID,
        targetUserId: UUID,
        role: CompanyRole
    ) {
        userActive(userId)
        if (userId == targetUserId) {
            logger.warn("User ${userId} attempted to change their own role in company ${companyId}")
            throw DomainException.Forbidden("Not allowed")
        }

        val currentUser = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")
        if (currentUser.role.canManageMembers()) {
            logger.warn("User ${userId} with role ${currentUser.role} attempted to change member role in company ${companyId}")
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
            ?: throw DomainException.NotFound("Member", targetUserId.toString())
        if (targetMember.role.isHigherThan(currentUser.role) || currentUser.role == targetMember.role) {
            logger.warn("User ${userId} attempted to change role of higher-level user ${targetUserId} in company ${companyId}")
            throw DomainException.BusinessRule("Admin cannot change another admin's role")
        }

        companyMemberRepository.updateRole(targetMember.id, role)

        eventProducer.publish(
            topic = KafkaTopics.COMPANY_MEMBER_ROLE_UPDATED,
            key = targetMember.id.toString(),
            event = CompanyMemberRoleUpdatedEvent(
                memberId = targetMember.id.toString(),
                companyId = companyId.toString(),
                userId = targetUserId.toString(),
                role = role.name
            )
        )

        logger.info("Member role updated: user ${userId} changed role of user ${targetUserId} to ${role} in company ${companyId}")
    }

    override suspend fun removeMember(
        companyId: UUID,
        userId: UUID,
        targetUserId: UUID
    ) {
        userActive(userId)
        val currentUser = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")
        val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
            ?: throw DomainException.NotFound("Member", targetUserId.toString())

        if (targetMember.role == CompanyRole.OWNER) {
            logger.warn("User ${userId} attempted to remove owner ${targetUserId} from company ${companyId}")
            throw DomainException.BusinessRule("Cannot remove owner")
        }
        if (targetMember.role.isAtLeast(currentUser.role) && currentUser.role.canManageMembers()) {
            logger.warn("User ${userId} with role ${currentUser.role} attempted to remove higher-level user ${targetUserId} from company ${companyId}")
            throw DomainException.AccessDenied("Admin can only remove members and guests")
        }

        companyMemberRepository.remove(targetMember.id)
        companyRepository.decrementUserCount(companyId)

        eventProducer.publish(
            topic = KafkaTopics.COMPANY_MEMBER_REMOVED,
            key = targetMember.id.toString(),
            event = CompanyMemberRemovedEvent(
                memberId = targetMember.id.toString()
            )
        )

        logger.info("Member removed: user ${userId} removed user ${targetUserId} from company ${companyId}")
    }

    override suspend fun leaveCompany(companyId: UUID, userId: UUID) {
        val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.NotFound("Member", userId.toString())
        if (member.role == CompanyRole.OWNER) {
            logger.warn("Owner ${userId} attempted to leave company ${companyId}")
            throw DomainException.BusinessRule("Owner cannot leave the company")
        }

        companyMemberRepository.remove(member.id)
        companyRepository.decrementUserCount(companyId)

        eventProducer.publish(
            topic = KafkaTopics.COMPANY_MEMBER_REMOVED,
            key = member.id.toString(),
            event = CompanyMemberRemovedEvent(
                memberId = member.id.toString()
            )
        )

        logger.info("User left company: $userId left company $companyId")
    }
}