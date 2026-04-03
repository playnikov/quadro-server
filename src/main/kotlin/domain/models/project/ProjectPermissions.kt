package com.quadro.domain.models.project

data class ProjectPermissions(
    val canEdit: Boolean,
    val canDelete: Boolean,
    val canManageMembers: Boolean,
    val canManageTeams: Boolean,
    val canCreateTasks: Boolean,
    val canEditAllTasks: Boolean,
    val canDeleteTasks: Boolean,
    val canChangeSettings: Boolean
) {
    companion object {
        fun fromRole(role: ProjectRole?): ProjectPermissions = when (role) {
            ProjectRole.OWNER -> ProjectPermissions(
                canEdit = true,
                canDelete = true,
                canManageMembers = true,
                canManageTeams = true,
                canCreateTasks = true,
                canEditAllTasks = true,
                canDeleteTasks = true,
                canChangeSettings = true
            )

            ProjectRole.LEAD -> ProjectPermissions(
                canEdit = true,
                canDelete = true,
                canManageMembers = true,
                canManageTeams = true,
                canCreateTasks = true,
                canEditAllTasks = true,
                canDeleteTasks = true,
                canChangeSettings = true
            )

            ProjectRole.ADMIN -> ProjectPermissions(
                canEdit = true,
                canDelete = false,
                canManageMembers = true,
                canManageTeams = true,
                canCreateTasks = true,
                canEditAllTasks = true,
                canDeleteTasks = true,
                canChangeSettings = true
            )

            ProjectRole.MEMBER -> ProjectPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canManageTeams = false,
                canCreateTasks = true,
                canEditAllTasks = false,
                canDeleteTasks = false,
                canChangeSettings = false
            )

            ProjectRole.VIEWER -> ProjectPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canManageTeams = false,
                canCreateTasks = false,
                canEditAllTasks = false,
                canDeleteTasks = false,
                canChangeSettings = false
            )

            null -> ProjectPermissions(
                canEdit = false,
                canDelete = false,
                canManageMembers = false,
                canManageTeams = false,
                canCreateTasks = false,
                canEditAllTasks = false,
                canDeleteTasks = false,
                canChangeSettings = false
            )
        }
    }
}