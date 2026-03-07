package com.quadro.datasource.mappers

import com.quadro.datasource.entities.ProjectEntity
import com.quadro.datasource.entities.ProjectMemberEntity
import com.quadro.datasource.entities.ProjectTeamEntity
import com.quadro.domain.models.project.Project
import com.quadro.domain.models.project.ProjectMember
import com.quadro.domain.models.project.ProjectPriority
import com.quadro.domain.models.project.ProjectRole
import com.quadro.domain.models.project.ProjectStatus
import com.quadro.domain.models.project.ProjectTeam
import com.quadro.domain.models.project.ProjectType
import com.quadro.domain.models.project.ProjectVisibility
import kotlinx.serialization.json.Json
import java.time.Instant

object ProjectMapper {
    val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: ProjectEntity): Project = Project(
        id = entity.id.value,
        companyId = entity.companyId,
        type = ProjectType.valueOf(entity.type),
        name = entity.name,
        key = entity.key,
        description = entity.description,
        status = ProjectStatus.valueOf(entity.status),
        priority = ProjectPriority.valueOf(entity.priority),
        visibility = ProjectVisibility.valueOf(entity.visibility),
        leadId = entity.leadId,
        ownerId = entity.ownerId,
        settings = json.decodeFromString(entity.settings),
        startDate = entity.startDate?.toEpochMilli(),
        endDate = entity.endDate?.toEpochMilli(),
        completedAt = entity.completedAt?.toEpochMilli(),
        createdAt = entity.createdAt.toEpochMilli(),
        updatedAt = entity.updatedAt.toEpochMilli(),
        archivedAt = entity.archivedAt?.toEpochMilli(),
    )

    fun toEntity(domain: Project): ProjectEntity = ProjectEntity.findById(domain.id) ?: ProjectEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: ProjectEntity, domain: Project) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: ProjectEntity, domain: Project) {
        entity.companyId = domain.companyId
        entity.type = domain.type.name
        entity.name = domain.name
        entity.key = domain.key
        entity.description = domain.description
        entity.status = domain.status.name
        entity.priority = domain.priority.name
        entity.visibility = domain.visibility.name
        entity.leadId = domain.leadId
        entity.ownerId = domain.ownerId
        entity.settings = json.encodeToString(domain.settings)
        entity.startDate = domain.startDate?.let { Instant.ofEpochMilli(it) }
        entity.endDate = domain.endDate?.let { Instant.ofEpochMilli(it) }
        entity.completedAt = domain.completedAt?.let { Instant.ofEpochMilli(it) }
        entity.createdAt = Instant.ofEpochMilli(domain.createdAt)
        entity.updatedAt = Instant.ofEpochMilli(domain.updatedAt)
        entity.archivedAt = domain.archivedAt?.let { Instant.ofEpochMilli(it) }
    }
}

object ProjectTeamMapper {
    fun toDomain(entity: ProjectTeamEntity): ProjectTeam = ProjectTeam(
        id = entity.id.value,
        projectId = entity.projectId,
        teamId = entity.teamId,
        role = ProjectRole.valueOf(entity.role),
        isLeadTeam = entity.isLeadTeam,
        assignedAt = entity.assignedAt.toEpochMilli(),
        assignedBy = entity.assignedBy
    )

    fun toEntity(domain: ProjectTeam): ProjectTeamEntity = ProjectTeamEntity.findById(domain.id) ?: ProjectTeamEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: ProjectTeamEntity, domain: ProjectTeam) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: ProjectTeamEntity, domain: ProjectTeam) {
        entity.projectId = domain.projectId
        entity.teamId = domain.teamId
        entity.role = domain.role.name
        entity.isLeadTeam = domain.isLeadTeam
        entity.assignedAt = Instant.ofEpochMilli(domain.assignedAt)
        entity.assignedBy = domain.assignedBy
    }
}

object ProjectMemberMapper {
    fun toDomain(entity: ProjectMemberEntity): ProjectMember = ProjectMember(
        id = entity.id.value,
        projectId = entity.projectId,
        userId = entity.userId,
        role = ProjectRole.valueOf(entity.role),
        joinedAt = entity.joinedAt.toEpochMilli(),
        invitedBy = entity.invitedBy,
        invitedAt = entity.invitedAt.toEpochMilli(),
        sourceTeamId = entity.sourceTeamId
    )

    fun toEntity(domain: ProjectMember): ProjectMemberEntity = ProjectMemberEntity.findById(domain.id) ?: ProjectMemberEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: ProjectMemberEntity, domain: ProjectMember) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: ProjectMemberEntity, domain: ProjectMember) {
        entity.projectId = domain.projectId
        entity.userId = domain.userId
        entity.role = domain.role.name
        entity.joinedAt = Instant.ofEpochMilli(domain.joinedAt)
        entity.invitedBy = domain.invitedBy
        entity.invitedAt = Instant.ofEpochMilli(domain.invitedAt)
        entity.sourceTeamId = domain.sourceTeamId
    }
}