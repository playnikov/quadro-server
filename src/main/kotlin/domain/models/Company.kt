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

data class CompanyPermissions(
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canManageMembers: Boolean,
    val canCreateProjects: Boolean,
    val canCreateTeams: Boolean,
    val canChangeSettings: Boolean,
    val canInvite: Boolean
) {
    companion object {
        fun fromRole(role: CompanyRole): CompanyPermissions = when (role) {
            CompanyRole.OWNER -> CompanyPermissions(
                canEdit = true,
                canDelete = true,
                canManageMembers = true,
                canCreateProjects = true,
                canCreateTeams = true,
                canChangeSettings = true,
                canInvite = true
            )
            CompanyRole.ADMIN -> CompanyPermissions(
                canEdit = true,
                canDelete = false,
                canManageMembers = true,
                canCreateProjects = true,
                canCreateTeams = true,
                canChangeSettings = true,
                canInvite = true
            )
            CompanyRole.MANAGER -> CompanyPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canCreateProjects = true,
                canCreateTeams = true,
                canChangeSettings = false,
                canInvite = true
            )
            CompanyRole.MEMBER -> CompanyPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canCreateProjects = false,
                canCreateTeams = false,
                canChangeSettings = false,
                canInvite = false
            )
            CompanyRole.GUEST -> CompanyPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canCreateProjects = false,
                canCreateTeams = false,
                canChangeSettings = false,
                canInvite = false
            )
        }
    }
}

data class CompanyResult(
    val id: String,
    val name: String,
    val description: String?,
    val logo: String?,
    val website: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val taxId: String?,
    val status: String,
    val ownerId: String,
    val settings: CompanySettingsResult,
    val createdAt: Long
) {
    companion object {
        fun fromCompany(company: Company): CompanyResult = CompanyResult(
            id = company.id.toString(),
            name = company.name,
            description = company.description,
            logo = company.logo,
            website = company.website,
            email = company.email,
            phone = company.phone,
            address = company.address,
            taxId = company.taxId,
            status = company.companyStatus.toString(),
            ownerId = company.ownerId.toString(),
            settings = CompanySettingsResult.fromCompanySettings(company.companySettings),
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