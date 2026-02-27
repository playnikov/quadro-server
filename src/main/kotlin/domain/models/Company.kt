package com.quadro.domain.models

import java.util.UUID

enum class CompanyRole {
    OWNER,
    ADMIN,
    MANAGER,
    MEMBER,
    GUEST
}

enum class CompanyStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED,
    PENDING
}

enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    EXPIRED,
    CANCELLED
}

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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

data class CompanySettings(
    val allowGuestAccess: Boolean = false,
    val requireEmailVerification: Boolean = true,
    val defaultUserRole: CompanyRole = CompanyRole.MEMBER,
    val projectCreationRole: CompanyRole = CompanyRole.MANAGER,
    val teamCreationRole: CompanyRole = CompanyRole.MANAGER,
    val invitationExpiryDays: Int = 7,
    val maxTeamsPerProject: Int = 10,
    val maxUsersPerTeam: Int = 50
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
    val settings: CompanySettings? = null
)

data class CompanyUpdate(
    val name: String? = null,
    val description: String? = null,
    val logo: String? = null,
    val website: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val taxId: String? = null,
    val settings: CompanySettings? = null,
    val status: CompanyStatus? = null
)

data class CompanyResult(
    val id: UUID,
    val name: String,
    val description: String?,
    val logo: String?,
    val website: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val taxId: String?,
    val status: CompanyStatus,
    val ownerId: UUID,
    val settings: CompanySettings,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromCompany(company: Company): CompanyResult = CompanyResult(
            id = company.id,
            name = company.name,
            description = company.description,
            logo = company.logo,
            website = company.website,
            email = company.email,
            phone = company.phone,
            address = company.address,
            taxId = company.taxId,
            status = company.companyStatus,
            ownerId = company.ownerId,
            settings = company.companySettings,
            createdAt = company.createdAt
        )
    }
}

data class CompanySettingsResult(
    val allowGuestAccess: Boolean,
    val requireEmailVerification: Boolean,
    val defaultUserRole: String,
    val projectCreationRole: String,
    val teamCreationRole: String,
    val invitationExpiryDays: Int,
    val maxTeamsPerProject: Int,
    val maxUsersPerTeam: Int
) {
    companion object {
        fun fromCompanySettings(settings: CompanySettings): CompanySettingsResult = CompanySettingsResult(
            allowGuestAccess = settings.allowGuestAccess,
            requireEmailVerification = settings.requireEmailVerification,
            defaultUserRole = settings.defaultUserRole.toString(),
            projectCreationRole = settings.projectCreationRole.toString(),
            teamCreationRole = settings.teamCreationRole.toString(),
            invitationExpiryDays = settings.invitationExpiryDays,
            maxTeamsPerProject = settings.maxTeamsPerProject,
            maxUsersPerTeam = settings.maxUsersPerTeam
        )
    }
}