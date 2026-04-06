package com.quadro.company.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class CompanyRole {
    GUEST,
    MEMBER,
    MANAGER,
    ADMIN,
    OWNER
}

@Serializable
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
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
    val maxUsers: Int = 10,
    val currentUsers: Int = 0,
    val maxProjects: Int = 3,
    val currentProjects: Int = 0
)

data class CompanySettings(
    val allowGuestAccess: Boolean = false,
    val requireEmailVerification: Boolean = true,
    val defaultUserRole: CompanyRole = CompanyRole.MEMBER,
    val projectCreationRole: CompanyRole = CompanyRole.MANAGER,
    val teamCreationRole: CompanyRole = CompanyRole.MANAGER,
    val invitationExpiryDays: Int = 7,
    val maxTeamsPerProject: Int = 10,
    val maxUsersPerTeam: Int = 50,
    val allowExternalInvites: Boolean = true,
    val requireInviteApproval: Boolean = false,
    val defaultTeamRole: CompanyRole = CompanyRole.MEMBER,
    val autoJoinDomain: String? = null
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
    val status: CompanyStatus? = null,
    val settings: CompanySettings? = null
)

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
    val companySettings: CompanySettingsResponse,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant? = null,
    val maxUsers: Int,
    val currentUsers: Int,
    val maxProjects: Int,
    val currentProjects: Int,
    val owner: UserResponse? = null
) {
    companion object {
        fun fromCompany(company: Company, user: User? = null): CompanyResponse = CompanyResponse(
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
            companySettings = CompanySettingsResponse.fromSettings(company.companySettings),
            createdAt = company.createdAt,
            updatedAt = company.updatedAt,
            deletedAt = company.deletedAt,
            maxUsers = company.maxUsers,
            currentUsers = company.currentUsers,
            maxProjects = company.maxProjects,
            currentProjects = company.currentProjects,
            owner = user?.let { UserResponse.fromUser(it) }
        )
    }
}

@Serializable
data class CompanySettingsResponse(
    val allowGuestAccess: Boolean,
    val requireEmailVerification: Boolean,
    val defaultUserRole: CompanyRole,
    val projectCreationRole: CompanyRole,
    val teamCreationRole: CompanyRole,
    val invitationExpiryDays: Int,
    val maxTeamsPerProject: Int,
    val maxUsersPerTeam: Int,
    val allowExternalInvites: Boolean,
    val requireInviteApproval: Boolean,
    val defaultTeamRole: CompanyRole,
    val autoJoinDomain: String?
) {
    companion object {
        fun fromSettings(settings: CompanySettings): CompanySettingsResponse {
            return CompanySettingsResponse(
                allowGuestAccess = settings.allowGuestAccess,
                requireEmailVerification = settings.requireEmailVerification,
                defaultUserRole = settings.defaultUserRole,
                projectCreationRole = settings.projectCreationRole,
                teamCreationRole = settings.teamCreationRole,
                invitationExpiryDays = settings.invitationExpiryDays,
                maxTeamsPerProject = settings.maxTeamsPerProject,
                maxUsersPerTeam = settings.maxUsersPerTeam,
                allowExternalInvites = settings.allowExternalInvites,
                requireInviteApproval = settings.requireInviteApproval,
                defaultTeamRole = settings.defaultTeamRole,
                autoJoinDomain = settings.autoJoinDomain
            )
        }
    }
}