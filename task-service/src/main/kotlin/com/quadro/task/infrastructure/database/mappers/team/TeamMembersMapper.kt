package com.quadro.task.infrastructure.database.mappers.team

import com.quadro.task.domain.models.team.TeamMember
import com.quadro.task.domain.models.team.TeamRole
import com.quadro.task.infrastructure.database.entities.team.TeamMembersEntity


object TeamMembersMapper {
    fun toDomain(entity: TeamMembersEntity): TeamMember = TeamMember(
        teamId = entity.teamId,
        userId = entity.userId,
        role = TeamRole.valueOf(entity.role),
        isActive = entity.isActive
    )

    fun toEntity(domain: TeamMember): TeamMembersEntity =
        TeamMembersEntity.new {
            this.teamId = domain.teamId
            this.userId = domain.userId
            this.role = domain.role.name
            this.isActive = domain.isActive
        }

    fun updateEntity(entity: TeamMembersEntity, domain: TeamMember) {
        entity.teamId = domain.teamId
        entity.userId = domain.userId
        entity.role = domain.role.name
        entity.isActive = domain.isActive
    }
}