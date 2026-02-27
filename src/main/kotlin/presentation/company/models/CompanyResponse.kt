package com.quadro.presentation.company.models

import com.quadro.domain.models.CompanyResult
import com.quadro.domain.models.CompanyRole
import com.quadro.domain.models.CompanyStatus
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val status: String,
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

        fun fromCompany(company: CompanyResult): CompanyResponse = CompanyResponse(
            id = company.id,
            name = company.name,
            description = company.description,
            logo = company.logo,
            website = company.website,
            email = company.email,
            phone = company.phone,
            address = company.address,
            taxId = company.taxId,
            status = company.status,
            ownerId = company.ownerId,
            settings = CompanySettings(
                allowGuestAccess = company.settings.allowGuestAccess,
                requireEmailVerification = company.settings.requireEmailVerification,
                defaultUserRole = company.settings.defaultUserRole,
                teamCreationRole = company.settings.teamCreationRole,
                invitationExpiryDays = company.settings.invitationExpiryDays,
                maxUsersPerTeam = company.settings.maxUsersPerTeam,
                maxTeamsPerProject = company.settings.maxTeamsPerProject,
                projectCreationRole = company.settings.projectCreationRole
            ),
            createdAt = formattedTime(company.createdAt)
        )
    }
}

@Serializable
data class CompanySettings(
    val allowGuestAccess: Boolean,
    val requireEmailVerification: Boolean,
    val defaultUserRole: String,
    val projectCreationRole: String,
    val teamCreationRole: String,
    val invitationExpiryDays: Int,
    val maxTeamsPerProject: Int,
    val maxUsersPerTeam: Int
)