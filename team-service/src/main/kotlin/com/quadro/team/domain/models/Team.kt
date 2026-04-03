package com.quadro.team.domain.models

import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Instant

@Serializable
enum class TeamRole {
    MEMBER, LEAD
}

@Serializable
enum class TeamStatus {
    ACTIVE, ARCHIVED, DISBAND
}

@Serializable
enum class TeamVisibility {
    PUBLIC, PRIVATE, HIDDEN
}

data class Team(
    val id: UUID,
    val companyId: UUID,
    val name: String,
    val description: String?,
    val avatar: String?,
    val status: TeamStatus,
    val visibility: TeamVisibility,
    val leadId: UUID,
    val settings: TeamSettings,
    val createdAt: Instant,
    val updatedAt: Instant,
    val archivedAt: Instant?,
    val maxMembers: Int,
    val currentMembers: Int
)

@Serializable
data class TeamSettings(
    val allowGuests: Boolean = false,
    val memberCanInvite: Boolean = false,
    val memberCanCreateProjects: Boolean = false,
    val requireLeadApproval: Boolean = true,
    val defaultMemberRole: TeamRole = TeamRole.MEMBER,
    val maxProjects: Int = 10,
    val autoArchiveDays: Int? = null
)

data class TeamCreate(
    val name: String,
    val description: String? = null,
    val avatar: String? = null,
    val leadId: String,
    val visibility: TeamVisibility = TeamVisibility.PUBLIC,
    val settings: TeamSettings = TeamSettings(),
    val initialMembers: List<UUID>? = null
)

data class TeamUpdate(
    val name: String? = null,
    val description: String? = null,
    val avatar: String? = null,
    val visibility: TeamVisibility? = null,
    val settings: TeamSettings? = null,
    val status: TeamStatus? = null
)

@Serializable
data class TeamResponse(
    val id: String,
    val companyId: String,
    val name: String,
    val description: String?,
    val avatar: String?,
    val status: TeamStatus,
    val visibility: TeamVisibility,
    val leadId: String,
    val settings: TeamSettings,
    val createdAt: Instant,
    val updatedAt: Instant,
    val archivedAt: Instant?,
    val maxMembers: Int,
    val currentMembers: Int
) {
    companion object {
        fun fromTeam(team: Team): TeamResponse = TeamResponse(
            id = team.id.toString(),
            companyId = team.companyId.toString(),
            name = team.name,
            description = team.description,
            avatar = team.avatar,
            status = team.status,
            visibility = team.visibility,
            leadId = team.leadId.toString(),
            settings = team.settings,
            createdAt = team.createdAt,
            updatedAt = team.updatedAt,
            archivedAt = team.archivedAt,
            maxMembers = team.maxMembers,
            currentMembers = team.currentMembers
        )
    }
}