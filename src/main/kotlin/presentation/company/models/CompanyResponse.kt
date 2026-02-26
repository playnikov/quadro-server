package com.quadro.presentation.company.models

import com.quadro.domain.models.Company
import com.quadro.domain.models.CompanyRole
import com.quadro.domain.models.CompanyStatus
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

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
    val status: CompanyStatus,
    val ownerId: String,
    val settings: CompanySettings,
    val createdAt: String
) {
    companion object {
        private fun formattedTime(time: Long): String {
            return Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
        }

        fun fromCompany(company: com.quadro.domain.models.CompanyResponse): CompanyResponse = CompanyResponse(
            id = company.id.toString(),
            name = company.name,
            description = company.description,
            logo = company.logo,
            website = company.website,
            email = company.email,
            phone = company.phone,
            address = company.address,
            taxId = company.taxId,
            status = company.status,
            ownerId = company.ownerId.toString(),
            settings = CompanySettings(
                allowGuestAccess = company.settings.allowGuestAccess,
                requireEmailVerification = company.settings.requireEmailVerification,
                defaultUserRole = company.settings.defaultUserRole,
                teamCreationRole = company.settings.teamCreationRole,
                invitationExpiryDays = company.settings.invitationExpiryDays,
                maxUsersPerTeam = company.settings.maxUsersPerTeam,
                maxTeamsPerProject = company.settings.maxTeamsPerProject
            ),
            createdAt = formattedTime(company.createdAt)
        )
    }
}

@Serializable
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