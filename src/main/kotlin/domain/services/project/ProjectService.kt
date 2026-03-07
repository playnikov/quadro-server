package com.quadro.domain.services.project

import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectCreate
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectStats
import com.quadro.domain.models.project.ProjectTeam
import com.quadro.domain.models.project.ProjectTeamAssignment
import com.quadro.domain.models.project.ProjectUpdate
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
}