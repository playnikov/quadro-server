package com.quadro.datasource.mappers

import com.quadro.datasource.entities.TeamEntity
import com.quadro.datasource.entities.TeamMemberEntity
import com.quadro.datasource.entities.TeamRoleDb
import com.quadro.datasource.entities.TeamStatusDb
import com.quadro.datasource.entities.TeamVisibilityDb
import com.quadro.domain.models.team.Team
import com.quadro.domain.models.team.TeamMember
import com.quadro.domain.models.team.TeamRole
import com.quadro.domain.models.team.TeamStatus
import com.quadro.domain.models.team.TeamVisibility
import kotlinx.serialization.json.Json
import java.time.Instant

object TeamMapper {
    private val json = Json { ignoreUnknownKeys = true }

    fun toDomain(entity: TeamEntity): Team = Team(
        id = entity.id.value,
        companyId = entity.companyId,
        name = entity.name,
        description = entity.description,
        avatar = entity.avatar,
        status = when (entity.status) {
            TeamStatusDb.ACTIVE -> TeamStatus.ACTIVE
            TeamStatusDb.ARCHIVED -> TeamStatus.ARCHIVED
            TeamStatusDb.DISBANDED -> TeamStatus.DISBANDED
        },
        visibility = when (entity.visibility) {
            TeamVisibilityDb.PUBLIC -> TeamVisibility.PUBLIC
            TeamVisibilityDb.PRIVATE -> TeamVisibility.PRIVATE
            TeamVisibilityDb.HIDDEN -> TeamVisibility.HIDDEN
        },
        leadId = entity.leadId,
        settings = json.decodeFromString(entity.settings),
        createdAt = entity.createdAt.toEpochMilli(),
        updatedAt = entity.updatedAt.toEpochMilli(),
        archivedAt = entity.archivedAt?.toEpochMilli(),
        currentMembers = entity.currentMembers
    )

    fun toEntity(domain: Team): TeamEntity = TeamEntity.findById(domain.id) ?: TeamEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: TeamEntity, domain: Team) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TeamEntity, domain: Team) {
        entity.companyId = domain.companyId
        entity.name = domain.name
        entity.description = domain.description
        entity.avatar = domain.avatar
        entity.status = when (domain.status) {
            TeamStatus.ACTIVE -> TeamStatusDb.ACTIVE
            TeamStatus.ARCHIVED -> TeamStatusDb.ARCHIVED
            TeamStatus.DISBANDED -> TeamStatusDb.DISBANDED
        }
        entity.visibility = when (domain.visibility) {
            TeamVisibility.PUBLIC -> TeamVisibilityDb.PUBLIC
            TeamVisibility.PRIVATE -> TeamVisibilityDb.PRIVATE
            TeamVisibility.HIDDEN -> TeamVisibilityDb.HIDDEN
        }
        entity.leadId = domain.leadId
        entity.settings = json.encodeToString(domain.settings)
        entity.createdAt = Instant.ofEpochMilli(domain.createdAt)
        entity.updatedAt = Instant.ofEpochMilli(domain.updatedAt)
        entity.archivedAt = domain.archivedAt?.let { Instant.ofEpochMilli(it) }
        entity.currentMembers = domain.currentMembers
    }
}

object TeamMemberMapper {

    fun toDomain(entity: TeamMemberEntity): TeamMember = TeamMember(
        id = entity.id.value,
        teamId = entity.teamId,
        userId = entity.userId,
        role = when (entity.role) {
            TeamRoleDb.LEAD -> TeamRole.LEAD
            TeamRoleDb.ADMIN -> TeamRole.ADMIN
            TeamRoleDb.MEMBER -> TeamRole.MEMBER
            TeamRoleDb.GUEST -> TeamRole.GUEST
        },
        joinedAt = entity.joinedAt.toEpochMilli(),
        invitedBy = entity.invitedBy,
        invitedAt = entity.invitedAt.toEpochMilli(),
        isActive = entity.isActive
    )

    fun toEntity(domain: TeamMember): TeamMemberEntity = TeamMemberEntity.findById(domain.id) ?: TeamMemberEntity.new(domain.id) {
        applyDomainToEntity(this, domain)
    }

    fun updateEntity(entity: TeamMemberEntity, domain: TeamMember) {
        applyDomainToEntity(entity, domain)
    }

    private fun applyDomainToEntity(entity: TeamMemberEntity, domain: TeamMember) {
        entity.teamId = domain.teamId
        entity.userId = domain.userId
        entity.role = when (domain.role) {
            TeamRole.LEAD -> TeamRoleDb.LEAD
            TeamRole.ADMIN -> TeamRoleDb.ADMIN
            TeamRole.MEMBER -> TeamRoleDb.MEMBER
            TeamRole.GUEST -> TeamRoleDb.GUEST
        }
        entity.joinedAt = Instant.ofEpochMilli(domain.joinedAt)
        entity.invitedBy = domain.invitedBy
        entity.invitedAt = Instant.ofEpochMilli(domain.invitedAt)
        entity.isActive = domain.isActive
    }
}