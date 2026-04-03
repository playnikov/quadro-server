package com.quadro.domain.services.project

import com.quadro.domain.models.project.AddProjectMembers
import com.quadro.domain.models.project.AssignTeam
import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectCreate
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectPermissions
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectStats
import com.quadro.domain.models.project.ProjectTeam
import com.quadro.domain.models.project.ProjectUpdate
import com.quadro.domain.models.project.UpdateTeamRole
import java.util.UUID

interface ProjectService {
    // Project CRUD
    suspend fun createProject(userId: UUID, request: ProjectCreate): Result<Project>
    suspend fun getProject(projectId: UUID, userId: UUID): Result<Project>
    suspend fun getProjectByKey(companyId: UUID, key: String, userId: UUID): Result<Project>
    suspend fun updateProject(projectId: UUID, userId: UUID, request: ProjectUpdate): Result<Project>
    suspend fun deleteProject(projectId: UUID, userId: UUID): Result<Unit>
    suspend fun archiveProject(projectId: UUID, userId: UUID): Result<Unit>
    suspend fun restoreProject(projectId: UUID, userId: UUID): Result<Unit>

    // Project listing
    suspend fun getCompanyProjects(companyId: UUID, userId: UUID, page: Int, size: Int): Result<List<Project>>
    suspend fun getUserProjects(userId: UUID, companyId: UUID?): Result<List<Project>>
    suspend fun getTeamProjects(teamId: UUID, userId: UUID): Result<List<Project>>
    suspend fun searchProjects(companyId: UUID, userId: UUID, query: String): Result<List<Project>>

    // Teams management
    suspend fun assignTeam(projectId: UUID, userId: UUID, request: AssignTeam): Result<ProjectTeam>
    suspend fun getAssignedTeams(projectId: UUID, userId: UUID): Result<List<ProjectTeam>>
    suspend fun updateTeamRole(projectId: UUID, userId: UUID, teamId: UUID, request: UpdateTeamRole): Result<Unit>
    suspend fun unassignTeam(projectId: UUID, userId: UUID, teamId: UUID): Result<Unit>

    // Members management
    suspend fun addMembers(projectId: UUID, userId: UUID, request: AddProjectMembers): Result<List<ProjectMember>>
    suspend fun getProjectMembers(projectId: UUID, userId: UUID, page: Int, size: Int): Result<List<ProjectMember>>
    suspend fun getProjectMember(projectId: UUID, userId: UUID, targetUserId: UUID): Result<ProjectMember>
    suspend fun updateMemberRole(projectId: UUID, userId: UUID, targetUserId: UUID, role: ProjectRole): Result<Unit>
    suspend fun removeMember(projectId: UUID, userId: UUID, targetUserId: UUID): Result<Unit>
    suspend fun leaveProject(projectId: UUID, userId: UUID): Result<Unit>

    // Stats
    suspend fun getProjectStats(projectId: UUID, userId: UUID): Result<ProjectStats>
    suspend fun getProjectPermissions(projectId: UUID, userId: UUID): Result<ProjectPermissions>
}