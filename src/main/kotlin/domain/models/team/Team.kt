package com.quadro.domain.models.team

import java.util.UUID

enum class TeamRole {
    LEAD, ADMIN, MEMBER, GUEST
}

enum class TeamStatus {
    ACTIVE, ARCHIVED, DISBANDED
}

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
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
    val currentMembers: Int
)

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
    val companyId: UUID,
    val name: String,
    val description: String? = null,
    val avatar: String? = null,
    val visibility: TeamVisibility = TeamVisibility.PRIVATE,
    val settings: TeamSettings? = null,
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

data class TeamResult(
    val id: String,
    val companyId: String,
    val companyName: String,
    val name: String,
    val description: String?,
    val avatar: String?,
    val status: String,
    val visibility: String,
    val leadId: String,
    val leadName: String,
    val leadEmail: String,
    val createdAt: Long,
    val currentMembers: Int,
    val memberCount: Int,
    val projectCount: Int,
    val isMember: Boolean,
    val userRole: String?,
    val permissions: TeamPermissions
) {
    companion object {
        fun fromTeam(
            team: Team,
            companyName: String,
            leadName: String,
            leadEmail: String,
            memberCount: Int,
            projectCount: Int,
            isMember: Boolean,
            userRole: TeamRole?,
            permissions: TeamPermissions
        ): TeamResult = TeamResult(
            id = team.id.toString(),
            companyId = team.companyId.toString(),
            companyName = companyName,
            name = team.name,
            description = team.description,
            avatar = team.avatar,
            status = team.status.toString(),
            visibility = team.visibility.toString(),
            leadId = team.leadId.toString(),
            leadName = leadName,
            leadEmail = leadEmail,
            createdAt = team.createdAt,
            currentMembers = team.currentMembers,
            memberCount = memberCount,
            projectCount = projectCount,
            isMember = isMember,
            userRole = userRole?.toString(),
            permissions = permissions
        )
    }
}

data class TeamPermissions(
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canManageMembers: Boolean,
    val canInvite: Boolean,
    val canCreateProjects: Boolean,
    val canChangeSettings: Boolean
) {
    companion object {
        fun fromRole(role: TeamRole?): TeamPermissions = when (role) {
            TeamRole.LEAD -> TeamPermissions(
                canEdit = true,
                canDelete = true,
                canManageMembers = true,
                canInvite = true,
                canCreateProjects = true,
                canChangeSettings = true
            )
            TeamRole.ADMIN -> TeamPermissions(
                canEdit = true,
                canDelete = false,
                canManageMembers = true,
                canInvite = true,
                canCreateProjects = true,
                canChangeSettings = true
            )
            TeamRole.MEMBER -> TeamPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canInvite = false,
                canCreateProjects = false,
                canChangeSettings = false
            )
            TeamRole.GUEST -> TeamPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canInvite = false,
                canCreateProjects = false,
                canChangeSettings = false
            )
            null -> TeamPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canInvite = false,
                canCreateProjects = false,
                canChangeSettings = false
            )
        }
    }
}