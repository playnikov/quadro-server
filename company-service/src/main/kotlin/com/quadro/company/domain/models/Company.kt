package com.quadro.company.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class CompanyRole {
    GUEST, MEMBER, MANAGER, ADMIN, OWNER;

    fun canManageInvitations() = this in setOf(OWNER, ADMIN, MANAGER)
    fun canManageMembers() = this in setOf(OWNER, ADMIN)
    fun canUpdateCompany() = this in setOf(OWNER, ADMIN)
    fun canCreateProjects() = this in setOf(OWNER, ADMIN, MANAGER)
    fun canCreateTeams() = this in setOf(OWNER, ADMIN, MANAGER)
    fun canViewReports() = this in setOf(OWNER, ADMIN)
    fun isAtLeast(other: CompanyRole) = ordinal >= other.ordinal
    fun isHigherThan(other: CompanyRole) = ordinal > other.ordinal
}

@Serializable
enum class CompanyStatus { ACTIVE, SUSPENDED, CLOSED, PENDING }

data class Company(
    val id: UUID,
    val name: String,
    val description: String?,
    val logo: String?,
    val website: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val taxId: String?,
    val companyStatus: CompanyStatus,
    val ownerId: UUID,
    val companySettings: CompanySettings,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
    val maxUsers: Int = 10,
    val currentUsers: Int = 0,
    val maxProjects: Int = 3,
    val currentProjects: Int = 0
) {
    fun hasAvailableUserSlot() = currentUsers < maxUsers
    fun hasAvailableProjectSlot() = currentProjects < maxProjects
    fun isActive() = companyStatus == CompanyStatus.ACTIVE
    fun isSuspended() = companyStatus == CompanyStatus.SUSPENDED
}

@Serializable
data class CompanySettings(
    val defaultMemberRole: CompanyRole = CompanyRole.MEMBER,
    val requireInviteApproval: Boolean = false,
    val allowExternalInvites: Boolean = true,
    val inviteExpiryDays: Int = 7,
    val projectCreationRole: CompanyRole = CompanyRole.MANAGER,
    val teamCreationRole: CompanyRole = CompanyRole.MANAGER,
    val taskCreationRole: CompanyRole = CompanyRole.MEMBER,
    val sprintManagementRole: CompanyRole = CompanyRole.MANAGER,
    val reportViewRole: CompanyRole = CompanyRole.ADMIN,
    val enableFileAttachments: Boolean = true,
    val enableComments: Boolean = true,
    val enableTimeTracking: Boolean = false,
    val maxAttachmentSizeMb: Int = 25
)

data class CompanyCreate(
    val name: String,
    val description: String? = null,
    val logo: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val taxId: String? = null,
    val settings: CompanySettings? = null,
) {
    fun validate() {
        require(name.isNotBlank()) { "Company name cannot be blank" }
        require(name.length in 2..100) { "Company name must be 2-100 chars" }
        email?.let { require(it.matches(Regex(".+@.+\\..+"))) { "Invalid email" } }
        website?.let { require(it.startsWith("http")) { "Website must start with http/https" } }
    }
}

data class CompanyUpdate(
    val name: String? = null,
    val description: String? = null,
    val logo: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val taxId: String? = null,
    val status: CompanyStatus? = null,
    val settings: CompanySettings? = null,
) {
    fun validate() {
        name?.let {
            require(it.isNotBlank()) { "Name cannot be blank" }
            require(it.length in 2..100) { "Name must be 2-100 chars" }
        }
        email?.let { require(it.matches(Regex(".+@.+\\..+"))) { "Invalid email" } }
    }
}

@Serializable
data class CompanyResponse(
    val id: String,
    val name: String,
    val description: String?,
    val logo: String?,
    val website: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val taxId: String?,
    val companyStatus: String,
    val companySettings: CompanySettings,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
    val maxUsers: Int,
    val currentUsers: Int,
    val maxProjects: Int,
    val currentProjects: Int,
    val owner: UserResponse? = null,
) {
    companion object {
        fun from(company: Company, owner: User? = null) = CompanyResponse(
            id = company.id.toString(),
            name = company.name,
            description = company.description,
            logo = company.logo,
            website = company.website,
            email = company.email,
            phone = company.phone,
            address = company.address,
            taxId = company.taxId,
            companyStatus = company.companyStatus.name,
            companySettings = company.companySettings,
            createdAt = company.createdAt,
            updatedAt = company.updatedAt,
            deletedAt = company.deletedAt,
            maxUsers = company.maxUsers,
            currentUsers = company.currentUsers,
            maxProjects = company.maxProjects,
            currentProjects = company.currentProjects,
            owner = owner?.let { UserResponse.from(it) },
        )
    }
}