package com.quadro.presentation.project.models

import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectPermissions
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectStats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class ProjectResponse(
    val id: String,
    val companyId: String,
    val companyName: String,
    val type: String,
    val name: String,
    val key: String,
    val description: String?,
    val status: String,
    val priority: String,
    val visibility: String,
    val leadId: String,
    val leadName: String,
    val leadEmail: String,
    val ownerId: String,
    val ownerName: String,
    val createdAt: String,
    val updatedAt: String,
    val teams: List<ProjectTeamInfoResponse>,
    val members: List<ProjectMemberInfoResponse>,
    val isMember: Boolean,
    val userRole: String?,
    val permissions: ProjectPermissionsResponse
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

        private fun formatTime(timestamp: Long): String {
            return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(dateFormatter)
        }

        fun fromProject(
            project: Project,
            companyName: String,
            leadName: String,
            leadEmail: String,
            ownerName: String,
            teams: List<ProjectTeamInfoResponse>,
            members: List<ProjectMemberInfoResponse>,
            isMember: Boolean,
            userRole: ProjectRole?,
            permissions: ProjectPermissions
        ): ProjectResponse = ProjectResponse(
            id = project.id.toString(),
            companyId = project.companyId.toString(),
            companyName = companyName,
            type = project.type.name,
            name = project.name,
            key = project.key,
            description = project.description,
            status = project.status.name,
            priority = project.priority.name,
            visibility = project.visibility.name,
            leadId = project.leadId.toString(),
            leadName = leadName,
            leadEmail = leadEmail,
            ownerId = project.ownerId.toString(),
            ownerName = ownerName,
            createdAt = formatTime(project.createdAt),
            updatedAt = formatTime(project.updatedAt),
            teams = teams,
            members = members,
            isMember = isMember,
            userRole = userRole?.name,
            permissions = ProjectPermissionsResponse.fromPermissions(permissions)
        )
    }
}

@Serializable
data class ProjectStatsResponse(
    val totalTasks: Int,
    val completedTasks: Int,
    val inProgressTasks: Int,
    val todoTasks: Int,
    val overdueTasks: Int,
    val totalMembers: Int,
    val totalTeams: Int,
    val completionRate: Int,
    val lastActivityAt: String?
) {
    companion object {
        fun fromStats(stats: ProjectStats): ProjectStatsResponse = ProjectStatsResponse(
            totalTasks = stats.totalTasks,
            completedTasks = stats.completedTasks,
            inProgressTasks = stats.inProgressTasks,
            todoTasks = stats.todoTasks,
            overdueTasks = stats.overdueTasks,
            totalMembers = stats.totalMembers,
            totalTeams = stats.totalTeams,
            completionRate = if (stats.totalTasks > 0) (stats.completedTasks * 100 / stats.totalTasks) else 0,
            lastActivityAt = stats.lastActivityAt?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
            }
        )
    }
}

@Serializable
data class ProjectTeamInfoResponse(
    val teamId: String,
    val teamName: String,
    val role: String,
    val isLeadTeam: Boolean,
    val memberCount: Int
)

@Serializable
data class ProjectMemberInfoResponse(
    val userId: String,
    val userEmail: String,
    val userName: String,
    val userAvatar: String?,
    val role: String,
    val fromTeam: String?
)

@Serializable
data class ProjectPermissionsResponse(
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
        fun fromPermissions(permissions: ProjectPermissions): ProjectPermissionsResponse = ProjectPermissionsResponse(
            canEdit = permissions.canEdit,
            canDelete = permissions.canDelete,
            canManageMembers = permissions.canManageMembers,
            canManageTeams = permissions.canManageTeams,
            canCreateTasks = permissions.canCreateTasks,
            canEditAllTasks = permissions.canEditAllTasks,
            canDeleteTasks = permissions.canDeleteTasks,
            canChangeSettings = permissions.canChangeSettings
        )
    }
}

@Serializable
data class ProjectTeamResponse(
    val projectId: String,
    val projectName: String,
    val teamId: String,
    val teamName: String,
    val role: String,
    val isLeadTeam: Boolean,
    val memberCount: Int,
    val assignedAt: String
)

@Serializable
data class ProjectMemberResponse(
    val id: String,
    val projectId: String,
    val userId: String,
    val userEmail: String,
    val userName: String,
    val userAvatar: String?,
    val role: String,
    val joinedAt: String,
    val invitedBy: String,
    val invitedByEmail: String,
    val sourceTeamId: String?,
    val sourceTeamName: String?,
    val canEdit: Boolean,
    val canRemove: Boolean
)

@Serializable
data class ProjectListResponse(
    val projects: List<ProjectResponse>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int
)