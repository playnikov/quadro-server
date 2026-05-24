package com.quadro.project.domain.repositories

import com.quadro.project.domain.models.ProjectMember
import com.quadro.project.domain.models.MemberRole
import java.util.UUID
import kotlin.time.Instant

interface ProjectMemberRepository {
    suspend fun findById(id: UUID): ProjectMember?
    suspend fun findByProjectAndUser(projectId: UUID, userId: UUID): ProjectMember?
    suspend fun findByProject(projectId: UUID, limit: Int, offset: Int): List<ProjectMember>
    suspend fun findByProjectAndRole(projectId: UUID, role: MemberRole): List<ProjectMember>
    suspend fun exists(projectId: UUID, userId: UUID): Boolean
    suspend fun add(member: ProjectMember): ProjectMember
    suspend fun remove(id: UUID)
    suspend fun updateRole(id: UUID, role: MemberRole)
    suspend fun countByProject(projectId: UUID): Long
    suspend fun findNewMembers(projectId: UUID, since: Instant): List<ProjectMember>
}