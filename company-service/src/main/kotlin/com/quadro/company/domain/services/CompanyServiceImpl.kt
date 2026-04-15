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

    override suspend fun createCompany(
        userId: UUID,
        request: CompanyCreate
    ): CompanyResponse {
        request.validate()

        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "User Not Found")

        if (companyRepository.existsByName(request.name)) {
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
                maxMembers = createdCompany.maxUsers,
                companyStatus = createdCompany.companyStatus.name,
                createTeamRole = createdCompany.companySettings.teamCreationRole.name
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
        return CompanyResponse.from(createdCompany, user)
    }

    override suspend fun getCompany(
        companyId: UUID,
        userId: UUID
    ): CompanyResponse {
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())
        companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")

        val owner = userRepository.findById(company.ownerId)
        return CompanyResponse.from(company, owner)
    }

    override suspend fun updateCompany(
        companyId: UUID,
        userId: UUID,
        request: CompanyUpdate
    ): CompanyResponse {
        val user = userRepository.findById(userId)
            ?: throw DomainException.NotFound("User", "User Not Found")
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())
        val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member")

        if (member.role != CompanyRole.OWNER && member.role != CompanyRole.ADMIN) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        if (request.name != null && request.name != company.name && companyRepository.existsByName(request.name)) {
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
                createTeamRole = updatedCompany.companySettings.teamCreationRole.name
            )
        )

        logger.info("Company updated: ${saved.name}")
        return CompanyResponse.from(saved, user)
    }

    override suspend fun deleteCompany(companyId: UUID, userId: UUID) {
        val company = companyRepository.findById(companyId)
            ?: throw DomainException.NotFound("Company", companyId.toString())
        if (company.ownerId != userId) {
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

        logger.info("Company deleted: $companyId by owner: $userId")
    }

    override suspend fun getUserCompanies(
        userId: UUID,
        page: Int,
        size: Int
    ): List<CompanyResponse> {
        val offset = (page - 1) * size
        val companies = companyRepository.findByUser(userId, size, offset)
        return companies.map { company ->
            val owner = userRepository.findById(company.ownerId)
            CompanyResponse.from(company, owner)
        }
    }

    override suspend fun getCompanyMembers(
        companyId: UUID,
        userId: UUID,
        page: Int,
        size: Int
    ): List<CompanyMemberResponse> {
        require(page >= 1) { "Page must be >= 1" }
        require(size in 1..100) { "Size must be between 1 and 100" }

        companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")

        val offset = (page - 1) * size
        return companyMemberRepository
            .findByCompany(companyId, size, offset)
            .map { CompanyMemberResponse.fromCompanyMember(it) }
    }

    override suspend fun updateMemberRole(
        companyId: UUID,
        userId: UUID,
        targetUserId: UUID,
        role: CompanyRole
    ) {
        if (userId == targetUserId) {
            throw DomainException.Forbidden("Not allowed")
        }

        val currentUser = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")
        if (currentUser.role.canManageMembers()) {
            throw DomainException.AccessDenied("Insufficient permissions")
        }

        val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
            ?: throw DomainException.NotFound("Member", targetUserId.toString())
        if (targetMember.role.isHigherThan(currentUser.role) || currentUser.role == targetMember.role) {
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

        logger.info("Member role updated: $targetUserId to $role")
    }

    override suspend fun removeMember(
        companyId: UUID,
        userId: UUID,
        targetUserId: UUID
    ) {
        val currentUser = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.AccessDenied("Not a member of this company")
        val targetMember = companyMemberRepository.findByCompanyAndUser(companyId, targetUserId)
            ?: throw DomainException.NotFound("Member", targetUserId.toString())

        if (targetMember.role == CompanyRole.OWNER) {
            throw DomainException.BusinessRule("Cannot remove owner")
        }
        if (targetMember.role.isAtLeast(currentUser.role) && currentUser.role.canManageMembers()) {
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

        logger.info("Member removed: $targetUserId")
    }

    override suspend fun leaveCompany(companyId: UUID, userId: UUID) {
        val member = companyMemberRepository.findByCompanyAndUser(companyId, userId)
            ?: throw DomainException.NotFound("Member", userId.toString())
        if (member.role == CompanyRole.OWNER) {
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

        logger.info("User left company: $userId")
    }
}