package com.quadro.datasource.repositories.project

import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectRole
import java.util.UUID

interface ProjectMemberRepository {
    suspend fun add(member: ProjectMember): ProjectMember
    suspend fun addAll(members: List<ProjectMember>): List<ProjectMember>

    suspend fun findById(id: UUID): ProjectMember?
    suspend fun findByProjectAndUser(projectId: UUID, userId: UUID): ProjectMember?
    suspend fun findByProject(projectId: UUID, limit: Int, offset: Int): List<ProjectMember>
    suspend fun findByUser(userId: UUID, companyId: UUID?): List<ProjectMember>
    suspend fun findByTeam(teamId: UUID, projectId: UUID?): List<ProjectMember>

    suspend fun updateRole(id: UUID, role: ProjectRole): Boolean
    suspend fun updateLastActive(id: UUID): Boolean

    suspend fun remove(id: UUID): Boolean
    suspend fun removeByProjectAndUser(projectId: UUID, userId: UUID): Boolean
    suspend fun removeAllByProject(projectId: UUID): Int
    suspend fun removeAllByTeam(teamId: UUID, projectId: UUID?): Int

    suspend fun countByProject(projectId: UUID): Long
    suspend fun countByUser(userId: UUID, companyId: UUID?): Long
    suspend fun exists(projectId: UUID, userId: UUID): Boolean

    suspend fun syncTeamMembers(projectId: UUID, teamId: UUID, role: ProjectRole): Pair<Int, Int>
}